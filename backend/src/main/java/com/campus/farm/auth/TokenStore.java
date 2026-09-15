package com.campus.farm.auth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {
  private final SecureRandom random = new SecureRandom();
  private final Map<String, User> tokens = new ConcurrentHashMap<>();

  public String issue(User user) {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    tokens.put(token, user);
    return token;
  }

  public Optional<User> find(String token) {
    return token == null ? Optional.empty() : Optional.ofNullable(tokens.get(token));
  }
}
