package com.campus.farm.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface LatestSnapshotRepository extends JpaRepository<LatestSnapshot, Long> {
  Optional<LatestSnapshot> findBySourceAndDeviceCodeAndMetric(String source, String deviceCode, String metric);

  @Modifying
  @Transactional
  @Query("update LatestSnapshot s set s.value = :value, s.sourceTimestamp = :sourceTimestamp, "
      + "s.receivedTimestamp = :receivedTimestamp where s.source = :source and s.deviceCode = :deviceCode "
      + "and s.metric = :metric and s.sourceTimestamp <= :sourceTimestamp")
  int updateWhenSourceTimestampNotAfter(@Param("source") String source,
      @Param("deviceCode") String deviceCode, @Param("metric") String metric,
      @Param("value") BigDecimal value, @Param("sourceTimestamp") Instant sourceTimestamp,
      @Param("receivedTimestamp") Instant receivedTimestamp);
}
