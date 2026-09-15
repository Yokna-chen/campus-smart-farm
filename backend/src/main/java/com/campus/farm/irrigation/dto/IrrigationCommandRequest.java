package com.campus.farm.irrigation.dto;

public class IrrigationCommandRequest {
  private Integer durationMinutes;
  private String reason;
  public Integer getDurationMinutes() { return durationMinutes; }
  public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
}
