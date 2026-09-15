package com.campus.farm.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import org.springframework.stereotype.Component;

/** Demo-only adapter. It uses no credentials and makes no external request. */
@Component
public class HydrawiseDemoAdapter implements NamedSnapshotPort {
  public String integrationName() { return "hydrawise"; }

  public java.util.List<ExternalSnapshot> readSnapshots() {
    return Collections.singletonList(new ExternalSnapshot("HYDRAWISE", "IRR-001", "watering",
        BigDecimal.ZERO, Instant.now()));
  }
}
