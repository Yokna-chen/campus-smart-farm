package com.campus.farm.irrigation;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "irrigation_runs")
public class IrrigationRun {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, length = 64) private String actor;
  @Column(nullable = false, length = 64) private String target;
  @Column(nullable = false, length = 16) private String action;
  @Column(nullable = false, length = 512) private String parameterSummary;
  @Column(nullable = false, length = 32) private String commandResult;
  @Column(nullable = false, length = 32) private String confirmation;
  @Column(nullable = false, length = 512) private String outcomeDetail;
  @Column(nullable = false) private Instant recordedAt;

  protected IrrigationRun() { }
  public IrrigationRun(String actor, String target, String action, String parameterSummary,
      String commandResult, String confirmation, String outcomeDetail) {
    this.actor = actor; this.target = target; this.action = action;
    this.parameterSummary = parameterSummary; this.commandResult = commandResult;
    this.confirmation = confirmation; this.outcomeDetail = outcomeDetail; this.recordedAt = Instant.now();
  }
  public Long getId() { return id; }
  public String getActor() { return actor; }
  public String getTarget() { return target; }
  public String getAction() { return action; }
  public String getParameterSummary() { return parameterSummary; }
  public String getCommandResult() { return commandResult; }
  public String getConfirmation() { return confirmation; }
  public String getOutcomeDetail() { return outcomeDetail; }
  public Instant getRecordedAt() { return recordedAt; }
}
