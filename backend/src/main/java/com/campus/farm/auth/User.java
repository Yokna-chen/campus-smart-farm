package com.campus.farm.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "farm_users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String username;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false, length = 32)
  private String role;

  protected User() { }

  public User(String username, String passwordHash, String role) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  @JsonIgnore
  public String getPasswordHash() { return passwordHash; }
  public String getRole() { return role; }
}
