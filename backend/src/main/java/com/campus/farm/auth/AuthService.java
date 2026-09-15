package com.campus.farm.auth;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository users, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
  }

  public Optional<User> authenticate(String username, String password) {
    if (username == null || password == null) return Optional.empty();
    return users.findByUsername(username)
        .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()));
  }
}
