package com.campus.farm.irrigation;

public class IrrigationDispatchResult {
  private final boolean accepted;
  private final String detail;
  private IrrigationDispatchResult(boolean accepted, String detail) { this.accepted = accepted; this.detail = detail; }
  public static IrrigationDispatchResult accepted(String detail) { return new IrrigationDispatchResult(true, detail); }
  public static IrrigationDispatchResult rejected(String detail) { return new IrrigationDispatchResult(false, detail); }
  public boolean isAccepted() { return accepted; }
  public String getDetail() { return detail; }
}
