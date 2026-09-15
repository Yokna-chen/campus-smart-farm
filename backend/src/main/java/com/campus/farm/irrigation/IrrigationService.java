package com.campus.farm.irrigation;

import com.campus.farm.audit.AuditService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IrrigationService {
  private final IrrigationZoneRepository zones;
  private final IrrigationRunRepository runs;
  private final AuditService audits;
  private final IrrigationCommandPort port;
  private final IrrigationOperationPolicy policy;
  private final Object commandLock = new Object();

  public IrrigationService(IrrigationZoneRepository zones, IrrigationRunRepository runs, AuditService audits,
      IrrigationCommandPort port, IrrigationOperationPolicy policy) {
    this.zones = zones; this.runs = runs; this.audits = audits; this.port = port; this.policy = policy;
  }

  @Transactional(noRollbackFor = {IrrigationBadRequestException.class, IrrigationConflictException.class,
      IrrigationForbiddenException.class})
  public IrrigationCommandResult command(String actor, String role, String zoneId, String actionValue,
      Integer durationMinutes, String reason) {
    synchronized (commandLock) {
      return commandLocked(actor, role, zoneId, actionValue, durationMinutes, reason);
    }
  }

  private IrrigationCommandResult commandLocked(String actor, String role, String zoneId, String actionValue,
      Integer durationMinutes, String reason) {
    IrrigationAction action = null;
    String target = auditTarget(zoneId);
    String parameters = safeParameterSummary(durationMinutes, reason);
    String requestedAction = auditAction(actionValue);
    try {
      requireControlRole(role);
      action = IrrigationAction.fromPath(actionValue);
      target = normalizeTarget(zoneId, action);
      validateReason(reason);
      if (action != IrrigationAction.START && durationMinutes != null) {
        throw new IrrigationBadRequestException("durationMinutes is valid only for start");
      }
      if (action == IrrigationAction.PAUSE && "all".equals(target)) {
        return pauseAll(actor, action, parameters);
      }
      IrrigationZone zone = zones.findByZoneId(target)
          .orElseThrow(() -> new IrrigationBadRequestException("unknown irrigation zone"));
      if (action == IrrigationAction.START) {
        policy.validateStart(zone, durationMinutes);
        rejectRunningConflict(target);
      }
      IrrigationZoneStatus before = port.readStatus(target);
      IrrigationDispatchResult dispatched = port.dispatch(new IrrigationCommand(target, action, durationMinutes));
      if (!dispatched.isAccepted()) return record(actor, target, action.name(), parameters, "REJECTED", "NOT_CONFIRMED",
          "remote command rejected");
      IrrigationZoneStatus after = port.readStatus(target);
      String confirmation = confirms(action, after) ? "CONFIRMED" : "PENDING";
      return record(actor, target, action.name(), parameters, "ACCEPTED", confirmation,
          "status confirmation completed");
    } catch (IrrigationBadRequestException | IrrigationConflictException | IrrigationForbiddenException expected) {
      record(actor, target, action == null ? requestedAction : action.name(), parameters,
          "REJECTED", "NOT_SENT", expected.getMessage());
      throw expected;
    } catch (RuntimeException failure) {
      record(actor, target, action == null ? requestedAction : action.name(), parameters,
          "FAILED", "NOT_CONFIRMED", "command processing failed");
      throw failure;
    }
  }

  private IrrigationCommandResult pauseAll(String actor, IrrigationAction action, String parameters) {
    List<IrrigationZone> persistedZones = zones.findAll();
    for (IrrigationZone zone : persistedZones) port.readStatus(zone.getZoneId());
    for (IrrigationZone zone : persistedZones) {
      IrrigationDispatchResult dispatched = port.dispatch(new IrrigationCommand(zone.getZoneId(), action, null));
      if (!dispatched.isAccepted()) return record(actor, "all", action.name(), parameters, "REJECTED", "NOT_CONFIRMED",
          "remote command rejected");
    }
    boolean confirmed = true;
    for (IrrigationZone zone : persistedZones) {
      if (port.readStatus(zone.getZoneId()) != IrrigationZoneStatus.PAUSED) confirmed = false;
    }
    return record(actor, "all", action.name(), parameters, "ACCEPTED", confirmed ? "CONFIRMED" : "PENDING",
        "status confirmation completed");
  }

  private void rejectRunningConflict(String target) {
    for (IrrigationZoneState state : port.readAllStatuses()) {
      if (state.getStatus() == IrrigationZoneStatus.RUNNING && !target.equals(state.getZoneId())) {
        throw new IrrigationConflictException("another irrigation zone is already running");
      }
      if (state.getStatus() == IrrigationZoneStatus.RUNNING && target.equals(state.getZoneId())) {
        throw new IrrigationConflictException("irrigation zone is already running");
      }
    }
  }

  private void requireControlRole(String role) {
    if (!"ADMIN".equals(role) && !"OPERATOR".equals(role)) {
      throw new IrrigationForbiddenException("irrigation control requires ADMIN or OPERATOR role");
    }
  }

  private String normalizeTarget(String zoneId, IrrigationAction action) {
    if (zoneId == null || zoneId.trim().isEmpty()) throw new IrrigationBadRequestException("zoneId is required");
    String target = zoneId.trim();
    if (target.length() > 64) throw new IrrigationBadRequestException("zoneId is too long");
    if ("all".equals(target) && action != IrrigationAction.PAUSE) {
      throw new IrrigationBadRequestException("all is valid only for pause");
    }
    return target;
  }

  private boolean confirms(IrrigationAction action, IrrigationZoneStatus status) {
    if (action == IrrigationAction.START) return status == IrrigationZoneStatus.RUNNING;
    if (action == IrrigationAction.STOP) return status == IrrigationZoneStatus.IDLE;
    return status == IrrigationZoneStatus.PAUSED;
  }

  private void validateReason(String reason) {
    String cleanReason = reason == null ? "" : reason.trim();
    if (cleanReason.length() > 200) throw new IrrigationBadRequestException("reason is too long");
  }

  private String safeParameterSummary(Integer durationMinutes, String reason) {
    String cleanReason = reason == null ? "" : reason.trim();
    if (cleanReason.length() > 200) cleanReason = cleanReason.substring(0, 200) + " [truncated]";
    return "durationMinutes=" + (durationMinutes == null ? "none" : durationMinutes)
        + "; reason=" + cleanReason;
  }

  private String auditAction(String actionValue) {
    if (actionValue == null || actionValue.trim().isEmpty()) return "UNKNOWN";
    String normalized = actionValue.trim().toUpperCase(java.util.Locale.ROOT);
    return normalized.length() > 16 ? normalized.substring(0, 16) : normalized;
  }

  private String auditTarget(String zoneId) {
    if (zoneId == null || zoneId.trim().isEmpty()) return "UNKNOWN";
    String normalized = zoneId.trim();
    return normalized.length() > 64 ? normalized.substring(0, 64) : normalized;
  }

  private IrrigationCommandResult record(String actor, String target, String action, String parameters,
      String commandResult, String confirmation, String detail) {
    runs.save(new IrrigationRun(actor, target, action, parameters, commandResult, confirmation, detail));
    audits.record(actor, target, action, parameters, commandResult, confirmation, detail);
    return new IrrigationCommandResult(commandResult, confirmation, detail);
  }
}
