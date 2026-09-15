package com.campus.farm.integration;

import java.util.List;

/** A vendor-neutral, read-only boundary for external telemetry retrieval. */
public interface SnapshotPort {
  List<ExternalSnapshot> readSnapshots();
}
