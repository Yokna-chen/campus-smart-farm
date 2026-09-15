package com.campus.farm.irrigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campus.farm.audit.AuditRecordRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "farm.integration.sync-enabled=false")
@Transactional
class IrrigationOperationServiceTest {
  private static final Clock NOON = Clock.fixed(Instant.parse("2026-09-14T12:00:00Z"), ZoneOffset.UTC);

  @Autowired IrrigationZoneRepository zones;
  @Autowired IrrigationRunRepository runs;
  @Autowired AuditRecordRepository audits;

  private RecordingPort port;
  private IrrigationService service;

  @BeforeEach void setUp() {
    audits.deleteAll();
    runs.deleteAll();
    zones.deleteAll();
    zones.save(new IrrigationZone("north", "North field", "Vegetable beds", 30,
        LocalTime.of(6, 0), LocalTime.of(18, 0)));
    zones.save(new IrrigationZone("south", "South field", "Orchard", 20,
        LocalTime.of(6, 0), LocalTime.of(18, 0)));
    port = new RecordingPort();
    service = new IrrigationService(zones, runs, new com.campus.farm.audit.AuditService(audits),
        port, new IrrigationOperationPolicy(NOON));
  }

  @Test void rejectsNonPositiveStartDurationAsBadRequestAndAuditsOutcome() {
    assertThatThrownBy(() -> service.command("operator", "OPERATOR", "north", "start", 0, "dry soil"))
        .isInstanceOf(IrrigationBadRequestException.class);

    assertThat(runs.findAll()).singleElement().satisfies(run -> {
      assertThat(run.getCommandResult()).isEqualTo("REJECTED");
      assertThat(run.getConfirmation()).isEqualTo("NOT_SENT");
    });
    assertThat(audits.findAll()).singleElement().satisfies(audit -> {
      assertThat(audit.getActor()).isEqualTo("operator");
      assertThat(audit.getAction()).isEqualTo("START");
      assertThat(audit.getCommandResult()).isEqualTo("REJECTED");
    });
  }

  @Test void rejectsDurationAbovePerZoneMaximumAsConflict() {
    assertThatThrownBy(() -> service.command("operator", "OPERATOR", "north", "start", 31, "dry soil"))
        .isInstanceOf(IrrigationConflictException.class);

    assertThat(port.commands).isEmpty();
    assertThat(runs.findAll()).singleElement().extracting(IrrigationRun::getCommandResult)
        .isEqualTo("REJECTED");
  }

  @Test void rejectsStartOutsideConfiguredOperationWindowAsConflict() {
    IrrigationService afterHours = new IrrigationService(zones, runs,
        new com.campus.farm.audit.AuditService(audits), port,
        new IrrigationOperationPolicy(Clock.fixed(Instant.parse("2026-09-14T20:00:00Z"), ZoneOffset.UTC)));

    assertThatThrownBy(() -> afterHours.command("operator", "OPERATOR", "north", "start", 15, "dry soil"))
        .isInstanceOf(IrrigationConflictException.class);

    assertThat(port.commands).isEmpty();
  }

  @Test void rejectsStartWhenAnotherZoneIsRunningAsConflict() {
    port.statuses.put("south", IrrigationZoneStatus.RUNNING);

    assertThatThrownBy(() -> service.command("operator", "OPERATOR", "north", "start", 15, "dry soil"))
        .isInstanceOf(IrrigationConflictException.class);

    assertThat(port.commands).isEmpty();
  }

  @Test void rejectsViewerControlAsForbiddenAndAuditsOutcome() {
    assertThatThrownBy(() -> service.command("viewer", "VIEWER", "north", "start", 15, "dry soil"))
        .isInstanceOf(IrrigationForbiddenException.class);

    assertThat(port.commands).isEmpty();
    assertThat(audits.findAll()).singleElement().satisfies(audit -> {
      assertThat(audit.getActor()).isEqualTo("viewer");
      assertThat(audit.getCommandResult()).isEqualTo("REJECTED");
    });
  }

  @Test void confirmsStatusAfterCommandInsteadOfTreatingDispatchAsExecution() {
    IrrigationCommandResult result = service.command("admin", "ADMIN", "north", "start", 15, "dry soil");

    assertThat(port.reads).containsExactly("north", "north");
    assertThat(result.getCommandResult()).isEqualTo("ACCEPTED");
    assertThat(result.getConfirmation()).isEqualTo("CONFIRMED");
    assertThat(runs.findAll()).singleElement().satisfies(run -> {
      assertThat(run.getActor()).isEqualTo("admin");
      assertThat(run.getAction()).isEqualTo("START");
      assertThat(run.getParameterSummary()).contains("durationMinutes=15").contains("reason=dry soil");
      assertThat(run.getCommandResult()).isEqualTo("ACCEPTED");
      assertThat(run.getConfirmation()).isEqualTo("CONFIRMED");
    });
    assertThat(audits.findAll()).singleElement().satisfies(audit -> {
      assertThat(audit.getTarget()).isEqualTo("north");
      assertThat(audit.getAction()).isEqualTo("START");
      assertThat(audit.getConfirmation()).isEqualTo("CONFIRMED");
    });
  }

  @Test void stopRequiresIdleAndPauseRequiresPausedConfirmation() {
    port.statuses.put("north", IrrigationZoneStatus.RUNNING);
    assertThat(service.command("admin", "ADMIN", "north", "stop", null, "manual stop").getConfirmation())
        .isEqualTo("CONFIRMED");
    assertThat(service.command("admin", "ADMIN", "north", "pause", null, "hold").getConfirmation())
        .isEqualTo("CONFIRMED");
    assertThat(port.commands).extracting(IrrigationCommand::getAction)
        .containsExactly(IrrigationAction.STOP, IrrigationAction.PAUSE);
    assertThat(port.statuses.get("north")).isEqualTo(IrrigationZoneStatus.PAUSED);
  }

  @Test void pauseAllDispatchesEveryPersistedZoneAndRequiresPausedStatus() {
    IrrigationCommandResult result = service.command("admin", "ADMIN", "all", "pause", null, "rain");
    assertThat(result.getConfirmation()).isEqualTo("CONFIRMED");
    assertThat(port.commands).extracting(IrrigationCommand::getZoneId).containsExactlyInAnyOrder("north", "south");
  }

  @Test void unknownPostDispatchStatusIsNotConfirmed() {
    port.postDispatchStatus = IrrigationZoneStatus.UNKNOWN;
    IrrigationCommandResult result = service.command("admin", "ADMIN", "north", "start", 15, "dry soil");
    assertThat(result.getConfirmation()).isEqualTo("PENDING");
  }

  @Test void remoteDispatchFailureIsRecordedWithoutLeakingAdapterDetail() {
    port.failure = true;
    IrrigationCommandResult result = service.command("admin", "ADMIN", "north", "start", 15, "dry soil");
    assertThat(result.getCommandResult()).isEqualTo("REJECTED");
    assertThat(result.getDetail()).doesNotContain("vendor-secret").hasSizeLessThanOrEqualTo(200);
    assertThat(audits.findAll()).singleElement().satisfies(audit ->
        assertThat(audit.getOutcomeDetail()).doesNotContain("vendor-secret"));
  }

  @Test void oversizedTargetIsRejectedAndStoredTargetFitsAuditColumn() {
    String oversized = new String(new char[100]).replace('\0', 'x');
    assertThatThrownBy(() -> service.command("admin", "ADMIN", oversized, "start", 15, "dry soil"))
        .isInstanceOf(IrrigationBadRequestException.class);
    assertThat(audits.findAll()).singleElement().satisfies(audit -> assertThat(audit.getTarget()).hasSizeLessThanOrEqualTo(64));
    assertThat(runs.findAll()).singleElement().satisfies(run -> assertThat(run.getTarget()).hasSizeLessThanOrEqualTo(64));
  }

  @Test void concurrentStartsAllowOnlyOneDispatch() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      Future<IrrigationCommandResult> first = pool.submit(() -> { start.await(); return service.command("admin", "ADMIN", "zone-1", "start", 15, "a"); });
      Future<IrrigationCommandResult> second = pool.submit(() -> { start.await(); return service.command("admin", "ADMIN", "zone-2", "start", 15, "b"); });
      start.countDown();
      int accepted = 0;
      try { if (first.get().getCommandResult().equals("ACCEPTED")) accepted++; } catch (Exception ignored) { }
      try { if (second.get().getCommandResult().equals("ACCEPTED")) accepted++; } catch (Exception ignored) { }
      assertThat(accepted).isEqualTo(1);
      assertThat(port.commands).hasSize(1);
      assertThat(runs.findAll()).hasSize(2);
      assertThat(audits.findAll()).hasSize(2);
    } finally { pool.shutdownNow(); }
  }

  private static final class RecordingPort implements IrrigationCommandPort {
    private final Map<String, IrrigationZoneStatus> statuses = new HashMap<>();
    private final java.util.List<String> reads = new java.util.ArrayList<>();
    private final java.util.List<IrrigationCommand> commands = new java.util.ArrayList<>();
    private IrrigationZoneStatus postDispatchStatus;
    private boolean failure;

    public IrrigationZoneStatus readStatus(String zoneId) {
      reads.add(zoneId);
      return statuses.getOrDefault(zoneId, IrrigationZoneStatus.IDLE);
    }

    public java.util.List<IrrigationZoneState> readAllStatuses() {
      return Arrays.asList(new IrrigationZoneState("north", statuses.getOrDefault("north", IrrigationZoneStatus.IDLE)),
          new IrrigationZoneState("south", statuses.getOrDefault("south", IrrigationZoneStatus.IDLE)),
          new IrrigationZoneState("zone-1", statuses.getOrDefault("zone-1", IrrigationZoneStatus.IDLE)),
          new IrrigationZoneState("zone-2", statuses.getOrDefault("zone-2", IrrigationZoneStatus.IDLE)));
    }

    public IrrigationDispatchResult dispatch(IrrigationCommand command) {
      commands.add(command);
      if (failure) return IrrigationDispatchResult.rejected("vendor-secret dispatch failure");
      if (command.getAction() == IrrigationAction.START) statuses.put(command.getZoneId(), IrrigationZoneStatus.RUNNING);
      if (command.getAction() == IrrigationAction.STOP) statuses.put(command.getZoneId(), IrrigationZoneStatus.IDLE);
      if (command.getAction() == IrrigationAction.PAUSE) statuses.put(command.getZoneId(), IrrigationZoneStatus.PAUSED);
      if (postDispatchStatus != null) statuses.put(command.getZoneId(), postDispatchStatus);
      return IrrigationDispatchResult.accepted("demo-dispatch");
    }
  }
}
