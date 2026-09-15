package com.campus.farm.irrigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Demo-only in-memory controller. It makes no external request and holds no credentials. */
@Component
public class HydrawiseDemoIrrigationCommandAdapter implements IrrigationCommandPort {
  private final Map<String, IrrigationZoneStatus> statuses = new ConcurrentHashMap<>();

  public IrrigationZoneStatus readStatus(String zoneId) {
    return statuses.getOrDefault(zoneId, IrrigationZoneStatus.IDLE);
  }

  public List<IrrigationZoneState> readAllStatuses() {
    List<IrrigationZoneState> states = new ArrayList<>();
    states.add(new IrrigationZoneState("zone-1", readStatus("zone-1")));
    states.add(new IrrigationZoneState("zone-2", readStatus("zone-2")));
    return states;
  }

  public IrrigationDispatchResult dispatch(IrrigationCommand command) {
    if (command.getAction() == IrrigationAction.START) statuses.put(command.getZoneId(), IrrigationZoneStatus.RUNNING);
    if (command.getAction() == IrrigationAction.STOP) statuses.put(command.getZoneId(), IrrigationZoneStatus.IDLE);
    if (command.getAction() == IrrigationAction.PAUSE) {
      if ("all".equals(command.getZoneId())) {
        for (String zone : new ArrayList<>(statuses.keySet())) statuses.put(zone, IrrigationZoneStatus.PAUSED);
      } else statuses.put(command.getZoneId(), IrrigationZoneStatus.PAUSED);
    }
    return IrrigationDispatchResult.accepted("demo controller accepted command");
  }
}
