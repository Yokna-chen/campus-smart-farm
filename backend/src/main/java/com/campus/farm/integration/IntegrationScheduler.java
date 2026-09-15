package com.campus.farm.integration;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IntegrationScheduler {
  private final IntegrationSyncService syncService;
  private final IntegrationPortRegistry portRegistry;
  private final boolean enabled;

  public IntegrationScheduler(IntegrationSyncService syncService, IntegrationPortRegistry portRegistry,
      @Value("${farm.integration.sync-enabled:true}") boolean enabled) {
    this.syncService = syncService; this.portRegistry = portRegistry; this.enabled = enabled;
  }

  @Scheduled(fixedDelayString = "${farm.integration.sync-delay-ms:300000}")
  public void synchronizeConfiguredDemoSources() {
    if (!enabled) return;
    for (Map.Entry<String, SnapshotPort> entry : portRegistry.ports().entrySet()) {
      syncService.sync(entry.getKey(), entry.getValue());
    }
  }
}
