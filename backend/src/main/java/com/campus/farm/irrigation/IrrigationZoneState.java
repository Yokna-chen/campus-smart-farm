package com.campus.farm.irrigation;

public class IrrigationZoneState {
  private final String zoneId;
  private final IrrigationZoneStatus status;
  public IrrigationZoneState(String zoneId, IrrigationZoneStatus status) {
    this.zoneId = zoneId; this.status = status;
  }
  public String getZoneId() { return zoneId; }
  public IrrigationZoneStatus getStatus() { return status; }
}
