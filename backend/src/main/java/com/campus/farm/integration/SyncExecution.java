package com.campus.farm.integration;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sync_executions")
public class SyncExecution {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, length = 64) private String integration;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private SyncStatus status;
  @Column(nullable = false) private int attempts;
  @Column(nullable = false) private Instant startedAt;
  @Column(nullable = false) private Instant completedAt;
  @Column(length = 1000) private String errorMessage;

  protected SyncExecution() { }
  public SyncExecution(String integration, SyncStatus status, int attempts, Instant startedAt, Instant completedAt,
      String errorMessage) {
    this.integration = integration; this.status = status; this.attempts = attempts; this.startedAt = startedAt;
    this.completedAt = completedAt; this.errorMessage = errorMessage;
  }
  public Long getId() { return id; }
  public String getIntegration() { return integration; }
  public SyncStatus getStatus() { return status; }
  public int getAttempts() { return attempts; }
  public Instant getStartedAt() { return startedAt; }
  public Instant getCompletedAt() { return completedAt; }
  public String getErrorMessage() { return errorMessage; }
}
