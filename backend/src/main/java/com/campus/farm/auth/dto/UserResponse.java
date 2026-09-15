package com.campus.farm.auth.dto;

import com.campus.farm.auth.User;

public class UserResponse {
  private final Long id;
  private final String username;
  private final String role;
  public UserResponse(User user) {
    this.id = user.getId(); this.username = user.getUsername(); this.role = user.getRole();
  }
  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getRole() { return role; }
}
