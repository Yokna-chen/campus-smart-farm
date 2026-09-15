package com.campus.farm.overview;

import com.campus.farm.integration.DataFreshness;
import java.math.BigDecimal;
import java.time.Instant;

public class OverviewResponse {
  private final BigDecimal currentPower;
  private final BigDecimal todayEnergy;
  private final BigDecimal totalEnergy;
  private final Instant photovoltaicUpdatedAt;
  private final DataFreshness photovoltaicFreshness;
  private final long zoneCount;
  private final long runningZoneCount;
  public OverviewResponse(BigDecimal currentPower, BigDecimal todayEnergy, BigDecimal totalEnergy,
      Instant photovoltaicUpdatedAt, DataFreshness photovoltaicFreshness, long zoneCount, long runningZoneCount) {
    this.currentPower = currentPower; this.todayEnergy = todayEnergy; this.totalEnergy = totalEnergy;
    this.photovoltaicUpdatedAt = photovoltaicUpdatedAt; this.photovoltaicFreshness = photovoltaicFreshness;
    this.zoneCount = zoneCount; this.runningZoneCount = runningZoneCount;
  }
  public BigDecimal getCurrentPower() { return currentPower; }
  public BigDecimal getTodayEnergy() { return todayEnergy; }
  public BigDecimal getTotalEnergy() { return totalEnergy; }
  public Instant getPhotovoltaicUpdatedAt() { return photovoltaicUpdatedAt; }
  public DataFreshness getPhotovoltaicFreshness() { return photovoltaicFreshness; }
  public long getZoneCount() { return zoneCount; }
  public long getRunningZoneCount() { return runningZoneCount; }
}
