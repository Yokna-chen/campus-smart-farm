package com.campus.farm.integration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class IntegrationPortRegistry {
  private final Map<String, SnapshotPort> ports = new LinkedHashMap<>();

  public IntegrationPortRegistry(List<NamedSnapshotPort> namedPorts) {
    for (NamedSnapshotPort port : namedPorts) ports.put(port.integrationName(), port);
  }

  public Map<String, SnapshotPort> ports() {
    return new LinkedHashMap<>(ports);
  }
}
