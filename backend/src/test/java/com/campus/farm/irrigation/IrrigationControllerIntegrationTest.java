package com.campus.farm.irrigation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campus.farm.audit.AuditRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "farm.integration.sync-enabled=false")
@AutoConfigureMockMvc
class IrrigationControllerIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired AuditRecordRepository audits;
  @Autowired IrrigationRunRepository runs;

  @Test void controlEndpointAllowsOperatorAndRejectsBadDuration() throws Exception {
    mvc.perform(post("/api/irrigation/zone-1/start")
        .header("Authorization", "Bearer " + login("operator", "operator123"))
        .contentType(MediaType.APPLICATION_JSON).content("{\"durationMinutes\":0,\"reason\":\"dry soil\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test void rejectedHttpCommandStillPersistsRunAndAuditRecords() throws Exception {
    long auditCount = audits.count();
    long runCount = runs.count();

    mvc.perform(post("/api/irrigation/zone-1/start")
        .header("Authorization", "Bearer " + login("operator", "operator123"))
        .contentType(MediaType.APPLICATION_JSON).content("{\"durationMinutes\":0,\"reason\":\"dry soil\"}"))
        .andExpect(status().isBadRequest());

    org.assertj.core.api.Assertions.assertThat(audits.count()).isEqualTo(auditCount + 1);
    org.assertj.core.api.Assertions.assertThat(runs.count()).isEqualTo(runCount + 1);
  }

  @Test void unsupportedActionStillPersistsRunAndAuditRecords() throws Exception {
    long auditCount = audits.count();
    long runCount = runs.count();

    mvc.perform(post("/api/irrigation/zone-1/invalid")
        .header("Authorization", "Bearer " + login("operator", "operator123"))
        .contentType(MediaType.APPLICATION_JSON).content("{\"durationMinutes\":15,\"reason\":\"dry soil\"}"))
        .andExpect(status().isBadRequest());

    org.assertj.core.api.Assertions.assertThat(audits.count()).isEqualTo(auditCount + 1);
    org.assertj.core.api.Assertions.assertThat(runs.count()).isEqualTo(runCount + 1);
    org.assertj.core.api.Assertions.assertThat(audits.findAll().get((int) auditCount).getAction()).isEqualTo("INVALID");
  }

  @Test void controlEndpointRejectsViewerRole() throws Exception {
    mvc.perform(post("/api/irrigation/zone-1/start")
        .header("Authorization", "Bearer " + login("viewer", "view123"))
        .contentType(MediaType.APPLICATION_JSON).content("{\"durationMinutes\":15,\"reason\":\"dry soil\"}"))
        .andExpect(status().isForbidden());
  }

  @Test void nullJsonBodyReturnsBadRequest() throws Exception {
    mvc.perform(post("/api/irrigation/zone-1/stop")
        .header("Authorization", "Bearer " + login("operator", "operator123")))
        .andExpect(status().isBadRequest());
  }

  private String login(String username, String password) throws Exception {
    MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk()).andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("token").asText();
  }
}
