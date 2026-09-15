package com.campus.farm.device;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
  Optional<Device> findByDeviceCode(String deviceCode);
  List<Device> findByType(String type);
}
