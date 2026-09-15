package com.campus.farm.integration;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncExecutionRepository extends JpaRepository<SyncExecution, Long> {
  Optional<SyncExecution> findTopByIntegrationOrderByStartedAtDesc(String integration);
}
