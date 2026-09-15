package com.campus.farm.irrigation;

import java.util.Locale;

public enum IrrigationAction {
  START, STOP, PAUSE;

  public static IrrigationAction fromPath(String value) {
    if (value == null) throw new IrrigationBadRequestException("action is required");
    try {
      return valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException invalid) {
      throw new IrrigationBadRequestException("unsupported irrigation action");
    }
  }
}
