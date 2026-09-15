package com.campus.farm.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @Test void loginReturnsOpaqueTokenAndSafeUser() throws Exception {
    MvcResult result = mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"operator\",\"password\":\"operator123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isString())
        .andExpect(jsonPath("$.user.username").value("operator"))
        .andExpect(jsonPath("$.user.role").value("OPERATOR"))
        .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
        .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    String token = body.get("token").asText();
    org.assertj.core.api.Assertions.assertThat(token).isNotBlank().doesNotContain("operator123");
  }

  @Test void loginRejectsInvalidPassword() throws Exception {
    mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"operator\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test void protectedEndpointRejectsMissingBearerToken() throws Exception {
    mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test void protectedEndpointRejectsMalformedBearerToken() throws Exception {
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer not-a-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test void adminEndpointRejectsOperatorAndAllowsAdmin() throws Exception {
    String operatorToken = login("operator", "operator123");
    mvc.perform(get("/api/auth/admin-test").header("Authorization", "Bearer " + operatorToken))
        .andExpect(status().isForbidden());

    String adminToken = login("admin", "admin123");
    mvc.perform(get("/api/auth/admin-test").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());
  }

  private String login(String username, String password) throws Exception {
    MvcResult result = mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk()).andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
  }
}
