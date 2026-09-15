package com.campus.farm.integration;

/** A snapshot port registered under the stable local integration name. */
public interface NamedSnapshotPort extends SnapshotPort {
  String integrationName();
}
