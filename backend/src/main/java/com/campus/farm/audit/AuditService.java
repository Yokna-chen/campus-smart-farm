package com.campus.farm.audit;

import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private final AuditRecordRepository records;

  public AuditService(AuditRecordRepository records) { this.records = records; }

  public void record(String actor, String target, String action, String parameterSummary,
      String commandResult, String confirmation, String outcomeDetail) {
    records.save(new AuditRecord(actor, target, action, parameterSummary, commandResult,
        confirmation, outcomeDetail));
  }
}
