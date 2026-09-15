package com.campus.farm.device;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "devices")
public class Device {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String deviceCode;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(nullable = false, length = 32)
  private String type;

  @Column(nullable = false, length = 32)
  private String status;

  protected Device() { }

  public Device(String deviceCode, String name, String type, String status) {
    this.deviceCode = deviceCode;
    this.name = name;
    this.type = type;
    this.status = status;
  }

  public Long getId() { return id; }
  public String getDeviceCode() { return deviceCode; }
  public String getName() { return name; }
  public String getType() { return type; }
  public String getStatus() { return status; }
}
