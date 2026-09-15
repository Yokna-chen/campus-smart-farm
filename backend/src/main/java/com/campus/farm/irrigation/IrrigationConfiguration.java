package com.campus.farm.irrigation;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IrrigationConfiguration {
  @Bean public IrrigationOperationPolicy irrigationOperationPolicy() {
    return new IrrigationOperationPolicy(Clock.systemDefaultZone());
  }
}
