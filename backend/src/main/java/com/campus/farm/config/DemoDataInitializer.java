package com.campus.farm.config;

import com.campus.farm.auth.User;
import com.campus.farm.auth.UserRepository;
import com.campus.farm.device.Device;
import com.campus.farm.device.DeviceRepository;
import com.campus.farm.irrigation.IrrigationZone;
import com.campus.farm.irrigation.IrrigationZoneRepository;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DemoDataInitializer {
  @Bean
  CommandLineRunner seedDemoData(UserRepository users, DeviceRepository devices, IrrigationZoneRepository zones,
      @Value("${farm.demo-seed-enabled:true}") boolean enabled) {
    return args -> {
      if (!enabled) return;
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      if (!users.findByUsername("admin").isPresent()) {
        users.save(new User("admin", encoder.encode("admin123"), "ADMIN"));
      }
      if (!users.findByUsername("operator").isPresent()) {
        users.save(new User("operator", encoder.encode("operator123"), "OPERATOR"));
      }
      if (!users.findByUsername("viewer").isPresent()) {
        users.save(new User("viewer", encoder.encode("view123"), "VIEWER"));
      }
      if (!devices.findByDeviceCode("PV-001").isPresent()) {
        devices.save(new Device("PV-001", "Solar Array", "PHOTOVOLTAIC", "ONLINE"));
      }
      if (!devices.findByDeviceCode("IRR-001").isPresent()) {
        devices.save(new Device("IRR-001", "Irrigation Controller", "IRRIGATION", "ONLINE"));
      }
      if (!zones.findByZoneId("zone-1").isPresent()) {
        zones.save(new IrrigationZone("zone-1", "Demo vegetable beds", "Vegetable beds", 30,
            LocalTime.of(6, 0), LocalTime.of(22, 0)));
      }
      if (!zones.findByZoneId("zone-2").isPresent()) {
        zones.save(new IrrigationZone("zone-2", "Demo orchard", "Orchard", 30,
            LocalTime.of(6, 0), LocalTime.of(22, 0)));
      }
    };
  }
}
