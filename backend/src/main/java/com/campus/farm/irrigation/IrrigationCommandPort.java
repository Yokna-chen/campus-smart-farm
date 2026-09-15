package com.campus.farm.irrigation;

import java.util.List;

/** Vendor boundary for reading controller state and dispatching irrigation commands. */
public interface IrrigationCommandPort {
  IrrigationZoneStatus readStatus(String zoneId);
  List<IrrigationZoneState> readAllStatuses();
  IrrigationDispatchResult dispatch(IrrigationCommand command);
}
