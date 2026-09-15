package com.campus.farm.audit;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
  private final AuditRecordRepository records;
  public AuditController(AuditRecordRepository records) { this.records = records; }
  @GetMapping public List<AuditRecord> list() { return records.findAll(); }
}
