package com.campus.farm.integration;

import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "latest_snapshots", uniqueConstraints = @UniqueConstraint(columnNames = {"source", "device_code", "metric"}))
public class LatestSnapshot {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, length = 32) private String source;
  @Column(name = "device_code", nullable = false, length = 64) private String deviceCode;
  @Column(nullable = false, length = 64) private String metric;
  @Column(name = "reading_value", nullable = false, precision = 19, scale = 4) private BigDecimal value;
  @Column(name = "source_timestamp", nullable = false) private Instant sourceTimestamp;
  @Column(name = "received_timestamp", nullable = false) private Instant receivedTimestamp;

  protected LatestSnapshot() { }
  public LatestSnapshot(ExternalSnapshot snapshot, Instant receivedTimestamp) {
    source = snapshot.getSource(); deviceCode = snapshot.getDeviceCode(); metric = snapshot.getMetric();
    value = snapshot.getValue(); sourceTimestamp = snapshot.getSourceTimestamp(); this.receivedTimestamp = receivedTimestamp;
  }
  public void updateFrom(ExternalSnapshot snapshot, Instant receivedTimestamp) {
    value = snapshot.getValue(); sourceTimestamp = snapshot.getSourceTimestamp(); this.receivedTimestamp = receivedTimestamp;
  }
  public Long getId() { return id; }
  public String getSource() { return source; }
  public String getDeviceCode() { return deviceCode; }
  public String getMetric() { return metric; }
  public BigDecimal getValue() { return value; }
  public Instant getSourceTimestamp() { return sourceTimestamp; }
  public Instant getReceivedTimestamp() { return receivedTimestamp; }
}
