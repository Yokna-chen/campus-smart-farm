package com.campus.farm.irrigation;

public class IrrigationCommand {
  private final String zoneId;
  private final IrrigationAction action;
  private final Integer durationMinutes;
  public IrrigationCommand(String zoneId, IrrigationAction action, Integer durationMinutes) {
    this.zoneId = zoneId; this.action = action; this.durationMinutes = durationMinutes;
  }
  public String getZoneId() { return zoneId; }
  public IrrigationAction getAction() { return action; }
  public Integer getDurationMinutes() { return durationMinutes; }
}
