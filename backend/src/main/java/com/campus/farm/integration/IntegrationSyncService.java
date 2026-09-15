package com.campus.farm.integration;

import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationSyncService {
  private static final int MAX_ATTEMPTS = 3;
  private final LatestSnapshotWriter snapshotWriter;
  private final SyncExecutionRepository executions;

  public IntegrationSyncService(LatestSnapshotWriter snapshotWriter, SyncExecutionRepository executions) {
    this.snapshotWriter = snapshotWriter;
    this.executions = executions;
  }

  @Transactional
  public void sync(String integration, SnapshotPort port) {
    Instant startedAt = Instant.now();
    int attempts = 0;
    try {
      List<ExternalSnapshot> retrieved = null;
      while (attempts < MAX_ATTEMPTS) {
        attempts++;
        try {
          retrieved = port.readSnapshots();
          break;
        } catch (RuntimeException exception) {
          if (attempts == MAX_ATTEMPTS) throw exception;
        }
      }
      Instant receivedAt = Instant.now();
      for (ExternalSnapshot snapshot : retrieved) saveLatest(snapshot, receivedAt);
      executions.save(new SyncExecution(integration, SyncStatus.SUCCESS, attempts, startedAt, Instant.now(), null));
    } catch (RuntimeException exception) {
      executions.save(new SyncExecution(integration, SyncStatus.FAILED, attempts, startedAt, Instant.now(), safeError(exception)));
    }
  }

  private String safeError(RuntimeException exception) {
    String message = exception.getMessage();
    if (message == null || message.trim().isEmpty()) message = exception.getClass().getSimpleName();
    message = message.replaceAll("(?i)(token|secret|key|password)=[^&\\s]+", "$1=[redacted]");
    return message.length() > 256 ? message.substring(0, 256) : message;
  }

  private void saveLatest(ExternalSnapshot external, Instant receivedAt) {
    try {
      snapshotWriter.writeIfNewest(external, receivedAt);
    } catch (DataIntegrityViolationException alreadyInserted) {
      // The competing insert rolled back in its own transaction. Re-evaluate against the committed row.
      snapshotWriter.writeIfNewest(external, receivedAt);
    }
  }
}
