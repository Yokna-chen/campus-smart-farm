package com.campus.farm.device;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
  private final DeviceRepository devices;
  public DeviceController(DeviceRepository devices) { this.devices = devices; }
  @GetMapping public List<Device> list() { return devices.findAll(); }
}
