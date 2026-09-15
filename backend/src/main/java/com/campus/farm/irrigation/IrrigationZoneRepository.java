package com.campus.farm.irrigation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IrrigationZoneRepository extends JpaRepository<IrrigationZone, Long> {
  Optional<IrrigationZone> findByZoneId(String zoneId);
}
