package com.campus.farm.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@SpringBootTest(properties = "farm.integration.sync-enabled=false")
@Transactional
class IntegrationSyncServiceTest {
  private static final Instant NOW = Instant.parse("2026-09-14T00:00:00Z");

  @Autowired IntegrationSyncService syncService;
  @Autowired LatestSnapshotRepository snapshots;
  @Autowired SyncExecutionRepository executions;

  @BeforeEach void clearIntegrationState() {
    executions.deleteAll();
    snapshots.deleteAll();
  }

  @Test void syncMapsPortSnapshotsAndPreservesBothTimestamps() {
    SnapshotPort port = () -> Collections.singletonList(new ExternalSnapshot(
        "FUSIONSOLAR", "PV-MAPPING", "powerKw", new BigDecimal("12.50"), NOW.minusSeconds(30)));

    syncService.sync("fusion-solar", port);

    LatestSnapshot saved = snapshots.findBySourceAndDeviceCodeAndMetric(
        "FUSIONSOLAR", "PV-MAPPING", "powerKw").orElseThrow(AssertionError::new);
    assertThat(saved.getValue()).isEqualByComparingTo("12.50");
    assertThat(saved.getSourceTimestamp()).isEqualTo(NOW.minusSeconds(30));
    assertThat(saved.getReceivedTimestamp()).isNotNull();
    assertThat(executions.findTopByIntegrationOrderByStartedAtDesc("fusion-solar").orElseThrow(AssertionError::new)
        .getStatus()).isEqualTo(SyncStatus.SUCCESS);
  }

  @Test void freshnessUsesSourceTimestampAndMarksOldDataDelayed() {
    DataFreshness freshness = new SnapshotFreshness(Clock.fixed(NOW, ZoneOffset.UTC), 60)
        .forSourceTimestamp(NOW.minusSeconds(61));

    assertThat(freshness).isEqualTo(DataFreshness.DELAYED);
  }

  @Test void syncCapsRetriesAndPersistsFailedExecution() {
    AtomicInteger calls = new AtomicInteger();
    SnapshotPort unavailable = () -> {
      calls.incrementAndGet();
      throw new SnapshotRetrievalException("upstream unavailable");
    };

    syncService.sync("hydrawise", unavailable);

    assertThat(calls.get()).isEqualTo(3);
    SyncExecution execution = executions.findTopByIntegrationOrderByStartedAtDesc("hydrawise")
        .orElseThrow(AssertionError::new);
    assertThat(execution.getStatus()).isEqualTo(SyncStatus.FAILED);
    assertThat(execution.getAttempts()).isEqualTo(3);
    assertThat(execution.getErrorMessage()).contains("upstream unavailable");
  }

  @Test void syncDoesNotReplaceNewerSnapshotWithAnOlderSourceTimestamp() {
    SnapshotPort current = () -> Collections.singletonList(new ExternalSnapshot(
        "FUSIONSOLAR", "PV-STALE", "powerKw", new BigDecimal("12.50"), NOW));
    SnapshotPort stale = () -> Collections.singletonList(new ExternalSnapshot(
        "FUSIONSOLAR", "PV-STALE", "powerKw", new BigDecimal("2.00"), NOW.minusSeconds(60)));

    syncService.sync("fusion-solar", current);
    syncService.sync("fusion-solar", stale);

    LatestSnapshot saved = snapshots.findBySourceAndDeviceCodeAndMetric(
        "FUSIONSOLAR", "PV-STALE", "powerKw").orElseThrow(AssertionError::new);
    assertThat(saved.getValue()).isEqualByComparingTo("12.50");
    assertThat(saved.getSourceTimestamp()).isEqualTo(NOW);
  }

  @Test void syncRecordsNormalRuntimeFailuresWithoutEscaping() {
    AtomicInteger calls = new AtomicInteger();
    SnapshotPort brokenAdapter = () -> {
      calls.incrementAndGet();
      throw new IllegalStateException("adapter mapping failed");
    };

    syncService.sync("fusion-solar", brokenAdapter);

    assertThat(calls.get()).isEqualTo(3);
    SyncExecution execution = executions.findTopByIntegrationOrderByStartedAtDesc("fusion-solar")
        .orElseThrow(AssertionError::new);
    assertThat(execution.getStatus()).isEqualTo(SyncStatus.FAILED);
    assertThat(execution.getAttempts()).isEqualTo(3);
    assertThat(execution.getErrorMessage()).contains("adapter mapping failed");
  }

  @Test void portRegistryExposesNamedSnapshotPortAbstractions() {
    NamedSnapshotPort fusion = new NamedSnapshotPort() {
      public String integrationName() { return "fusion-solar"; }
      public java.util.List<ExternalSnapshot> readSnapshots() { return Collections.emptyList(); }
    };
    NamedSnapshotPort hydrawise = new NamedSnapshotPort() {
      public String integrationName() { return "hydrawise"; }
      public java.util.List<ExternalSnapshot> readSnapshots() { return Collections.emptyList(); }
    };

    Map<String, SnapshotPort> ports = new IntegrationPortRegistry(Arrays.asList(fusion, hydrawise)).ports();

    assertThat(ports).containsEntry("fusion-solar", fusion).containsEntry("hydrawise", hydrawise);
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void concurrentTimestampUpdatesCannotLetAnOlderSourceTimestampWin() throws Exception {
    String deviceCode = "PV-CONCURRENT";
    snapshots.saveAndFlush(new LatestSnapshot(new ExternalSnapshot("FUSIONSOLAR", deviceCode,
        "powerKw", new BigDecimal("1.00"), NOW.minusSeconds(90)), NOW));
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      Future<?> fresh = pool.submit(() -> updateAfterStart(ready, start, deviceCode,
          new BigDecimal("12.50"), NOW));
      Future<?> stale = pool.submit(() -> updateAfterStart(ready, start, deviceCode,
          new BigDecimal("2.00"), NOW.minusSeconds(60)));
      ready.await();
      start.countDown();
      fresh.get();
      stale.get();
    } finally {
      pool.shutdownNow();
    }

    LatestSnapshot saved = snapshots.findBySourceAndDeviceCodeAndMetric(
        "FUSIONSOLAR", deviceCode, "powerKw").orElseThrow(AssertionError::new);
    assertThat(saved.getValue()).isEqualByComparingTo("12.50");
    assertThat(saved.getSourceTimestamp()).isEqualTo(NOW);
  }

  private void updateAfterStart(CountDownLatch ready, CountDownLatch start, String deviceCode,
      BigDecimal value, Instant sourceTimestamp) {
    ready.countDown();
    try {
      start.await();
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new AssertionError(interrupted);
    }
    syncService.sync("concurrent", () -> Collections.singletonList(new ExternalSnapshot("FUSIONSOLAR",
        deviceCode, "powerKw", value, sourceTimestamp)));
  }
}
