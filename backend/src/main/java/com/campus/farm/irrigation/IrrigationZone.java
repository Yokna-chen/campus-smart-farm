package com.campus.farm.irrigation;

import java.time.LocalTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "irrigation_zones")
public class IrrigationZone {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, unique = true, length = 64) private String zoneId;
  @Column(nullable = false, length = 128) private String name;
  @Column(nullable = false, length = 128) private String plantingArea;
  @Column(nullable = false) private int maximumDurationMinutes;
  @Column(nullable = false) private LocalTime operationWindowStart;
  @Column(nullable = false) private LocalTime operationWindowEnd;

  protected IrrigationZone() { }
  public IrrigationZone(String zoneId, String name, String plantingArea, int maximumDurationMinutes,
      LocalTime operationWindowStart, LocalTime operationWindowEnd) {
    this.zoneId = zoneId; this.name = name; this.plantingArea = plantingArea;
    this.maximumDurationMinutes = maximumDurationMinutes;
    this.operationWindowStart = operationWindowStart; this.operationWindowEnd = operationWindowEnd;
  }
  public Long getId() { return id; }
  public String getZoneId() { return zoneId; }
  public String getName() { return name; }
  public String getPlantingArea() { return plantingArea; }
  public int getMaximumDurationMinutes() { return maximumDurationMinutes; }
  public LocalTime getOperationWindowStart() { return operationWindowStart; }
  public LocalTime getOperationWindowEnd() { return operationWindowEnd; }
}
