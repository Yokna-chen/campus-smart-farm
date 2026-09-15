package com.campus.farm;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.campus.farm.auth.User;
import com.campus.farm.auth.UserRepository;
import com.campus.farm.device.Device;
import com.campus.farm.device.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
@Transactional
class DomainPersistenceTest {
  @Autowired UserRepository users;
  @Autowired DeviceRepository devices;

  @Test void seedCreatesDemoUsersAndDevices() {
    User admin = users.findByUsername("admin").orElseThrow(AssertionError::new);
    User operator = users.findByUsername("operator").orElseThrow(AssertionError::new);
    assertThat(admin.getRole()).isEqualTo("ADMIN");
    assertThat(new BCryptPasswordEncoder().matches("admin123", admin.getPasswordHash())).isTrue();
    assertThat(operator.getRole()).isEqualTo("OPERATOR");
    assertThat(new BCryptPasswordEncoder().matches("operator123", operator.getPasswordHash())).isTrue();

    assertSeedDevice("PV-001", "Solar Array", "PHOTOVOLTAIC", "ONLINE");
    assertSeedDevice("IRR-001", "Irrigation Controller", "IRRIGATION", "ONLINE");
  }

  @Test void passwordHashIsNeverSerialized() throws Exception {
    String json = new ObjectMapper().writeValueAsString(new User("operator", "secret-hash", "OPERATOR"));
    assertThat(json).doesNotContain("passwordHash").doesNotContain("secret-hash");
  }

  private void assertSeedDevice(String code, String name, String type, String status) {
    Device device = devices.findByDeviceCode(code).orElseThrow(AssertionError::new);
    assertThat(device.getDeviceCode()).isEqualTo(code);
    assertThat(device.getName()).isEqualTo(name);
    assertThat(device.getType()).isEqualTo(type);
    assertThat(device.getStatus()).isEqualTo(status);
  }
}
