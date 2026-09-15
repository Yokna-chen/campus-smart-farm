package com.campus.farm.irrigation;

public class IrrigationCommandResult {
  private final String commandResult;
  private final String confirmation;
  private final String detail;
  public IrrigationCommandResult(String commandResult, String confirmation, String detail) {
    this.commandResult = commandResult; this.confirmation = confirmation; this.detail = detail;
  }
  public String getCommandResult() { return commandResult; }
  public String getConfirmation() { return confirmation; }
  public String getDetail() { return detail; }
}
