package com.campus.farm.irrigation.dto;

import com.campus.farm.irrigation.IrrigationCommandResult;

public class IrrigationCommandResponse {
  private final String commandResult;
  private final String confirmation;
  private final String detail;
  public IrrigationCommandResponse(IrrigationCommandResult result) {
    commandResult = result.getCommandResult(); confirmation = result.getConfirmation(); detail = result.getDetail();
  }
  public String getCommandResult() { return commandResult; }
  public String getConfirmation() { return confirmation; }
  public String getDetail() { return detail; }
}
