package com.campus.farm.integration;

import java.math.BigDecimal;
import java.time.Instant;

public class ExternalSnapshot {
  private final String source;
  private final String deviceCode;
  private final String metric;
  private final BigDecimal value;
  private final Instant sourceTimestamp;

  public ExternalSnapshot(String source, String deviceCode, String metric, BigDecimal value,
      Instant sourceTimestamp) {
    this.source = source;
    this.deviceCode = deviceCode;
    this.metric = metric;
    this.value = value;
    this.sourceTimestamp = sourceTimestamp;
  }

  public String getSource() { return source; }
  public String getDeviceCode() { return deviceCode; }
  public String getMetric() { return metric; }
  public BigDecimal getValue() { return value; }
  public Instant getSourceTimestamp() { return sourceTimestamp; }
}
