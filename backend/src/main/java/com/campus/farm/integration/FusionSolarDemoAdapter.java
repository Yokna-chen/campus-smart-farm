package com.campus.farm.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import org.springframework.stereotype.Component;

/** Demo-only adapter. It uses no credentials and makes no external request. */
@Component
public class FusionSolarDemoAdapter implements NamedSnapshotPort {
  public String integrationName() { return "fusion-solar"; }

  public java.util.List<ExternalSnapshot> readSnapshots() {
    return Collections.singletonList(new ExternalSnapshot("FUSIONSOLAR", "PV-001", "powerKw",
        new BigDecimal("8.40"), Instant.now()));
  }
}
