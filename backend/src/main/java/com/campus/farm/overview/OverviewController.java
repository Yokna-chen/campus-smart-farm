package com.campus.farm.overview;

import com.campus.farm.integration.DataFreshness;
import com.campus.farm.integration.LatestSnapshot;
import com.campus.farm.integration.LatestSnapshotRepository;
import com.campus.farm.integration.SnapshotFreshness;
import com.campus.farm.irrigation.IrrigationCommandPort;
import com.campus.farm.irrigation.IrrigationZoneRepository;
import com.campus.farm.irrigation.IrrigationZoneStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OverviewController {
  private final LatestSnapshotRepository snapshots;
  private final IrrigationZoneRepository zones;
  private final IrrigationCommandPort irrigation;
  private final SnapshotFreshness freshness;
  public OverviewController(LatestSnapshotRepository snapshots, IrrigationZoneRepository zones,
      IrrigationCommandPort irrigation) {
    this.snapshots = snapshots; this.zones = zones; this.irrigation = irrigation;
    this.freshness = new SnapshotFreshness(Clock.systemUTC(), 900);
  }
  @GetMapping("/overview")
  public OverviewResponse overview() {
    Optional<LatestSnapshot> power = snapshots.findBySourceAndDeviceCodeAndMetric("FUSIONSOLAR", "PV-001", "powerKw");
    Optional<LatestSnapshot> today = snapshots.findBySourceAndDeviceCodeAndMetric("FUSIONSOLAR", "PV-001", "todayEnergy");
    Optional<LatestSnapshot> total = snapshots.findBySourceAndDeviceCodeAndMetric("FUSIONSOLAR", "PV-001", "totalEnergy");
    LatestSnapshot latest = power.orElse(null);
    long running = zones.findAll().stream().filter(z -> irrigation.readStatus(z.getZoneId()) == IrrigationZoneStatus.RUNNING).count();
    return new OverviewResponse(value(power), value(today), value(total), latest == null ? null : latest.getSourceTimestamp(),
        latest == null ? DataFreshness.UNKNOWN : freshness.forSourceTimestamp(latest.getSourceTimestamp()), zones.count(), running);
  }
  private BigDecimal value(Optional<LatestSnapshot> snapshot) { return snapshot.isPresent() ? snapshot.get().getValue() : null; }
}
