package com.campus.farm.integration;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LatestSnapshotWriter {
  private final LatestSnapshotRepository snapshots;

  public LatestSnapshotWriter(LatestSnapshotRepository snapshots) {
    this.snapshots = snapshots;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void writeIfNewest(ExternalSnapshot external, Instant receivedAt) {
    int updated = snapshots.updateWhenSourceTimestampNotAfter(external.getSource(), external.getDeviceCode(),
        external.getMetric(), external.getValue(), external.getSourceTimestamp(), receivedAt);
    if (updated == 0 && !snapshots.findBySourceAndDeviceCodeAndMetric(
        external.getSource(), external.getDeviceCode(), external.getMetric()).isPresent()) {
      snapshots.saveAndFlush(new LatestSnapshot(external, receivedAt));
    }
  }
}
