package com.campus.farm.auth;

import com.campus.farm.auth.dto.LoginRequest;
import com.campus.farm.auth.dto.LoginResponse;
import com.campus.farm.auth.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;
  private final TokenStore tokens;
  public AuthController(AuthService auth, TokenStore tokens) { this.auth = auth; this.tokens = tokens; }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    return auth.authenticate(request.getUsername(), request.getPassword())
        .map(user -> ResponseEntity.ok(new LoginResponse(tokens.issue(user), new UserResponse(user))))
        .orElseGet(() -> ResponseEntity.status(401).build());
  }

  @GetMapping("/me")
  public UserResponse me(Authentication authentication) {
    return new UserResponse((User) authentication.getDetails());
  }

  @GetMapping("/admin-test")
  public String adminTest() { return "ok"; }
}
