package com.campus.farm.irrigation.dto;

import com.campus.farm.irrigation.IrrigationZone;
import com.campus.farm.irrigation.IrrigationZoneStatus;
import java.time.LocalTime;

public class IrrigationZoneResponse {
  private final String id;
  private final String name;
  private final String plantingArea;
  private final int maximumDurationMinutes;
  private final LocalTime operationWindowStart;
  private final LocalTime operationWindowEnd;
  private final IrrigationZoneStatus status;
  public IrrigationZoneResponse(IrrigationZone zone, IrrigationZoneStatus status) {
    id = zone.getZoneId(); name = zone.getName(); plantingArea = zone.getPlantingArea();
    maximumDurationMinutes = zone.getMaximumDurationMinutes();
    operationWindowStart = zone.getOperationWindowStart(); operationWindowEnd = zone.getOperationWindowEnd();
    this.status = status;
  }
  public String getId() { return id; }
  public String getName() { return name; }
  public String getPlantingArea() { return plantingArea; }
  public int getMaximumDurationMinutes() { return maximumDurationMinutes; }
  public LocalTime getOperationWindowStart() { return operationWindowStart; }
  public LocalTime getOperationWindowEnd() { return operationWindowEnd; }
  public IrrigationZoneStatus getStatus() { return status; }
}
