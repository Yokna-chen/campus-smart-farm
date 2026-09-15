package com.campus.farm.irrigation;

import java.time.Clock;
import java.time.LocalTime;

public class IrrigationOperationPolicy {
  private final Clock clock;
  public IrrigationOperationPolicy(Clock clock) { this.clock = clock; }

  public void validateStart(IrrigationZone zone, Integer durationMinutes) {
    if (durationMinutes == null || durationMinutes <= 0) {
      throw new IrrigationBadRequestException("start durationMinutes must be positive");
    }
    if (durationMinutes > zone.getMaximumDurationMinutes()) {
      throw new IrrigationConflictException("requested duration exceeds zone maximum");
    }
    LocalTime now = LocalTime.now(clock);
    if (now.isBefore(zone.getOperationWindowStart()) || !now.isBefore(zone.getOperationWindowEnd())) {
      throw new IrrigationConflictException("operation is outside the configured window");
    }
  }
}
