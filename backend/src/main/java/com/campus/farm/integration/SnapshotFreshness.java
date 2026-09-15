package com.campus.farm.integration;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class SnapshotFreshness {
  private final Clock clock;
  private final long maximumAgeSeconds;

  public SnapshotFreshness(Clock clock, long maximumAgeSeconds) {
    this.clock = clock;
    this.maximumAgeSeconds = maximumAgeSeconds;
  }

  public DataFreshness forSourceTimestamp(Instant sourceTimestamp) {
    if (sourceTimestamp == null) return DataFreshness.UNKNOWN;
    if (sourceTimestamp.isAfter(clock.instant())) return DataFreshness.UNKNOWN;
    return Duration.between(sourceTimestamp, clock.instant()).getSeconds() <= maximumAgeSeconds
        ? DataFreshness.CURRENT : DataFreshness.DELAYED;
  }
}
