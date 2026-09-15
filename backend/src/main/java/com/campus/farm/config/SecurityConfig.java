package com.campus.farm.config;

import com.campus.farm.auth.BearerTokenFilter;
import com.campus.farm.auth.TokenStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  private final TokenStore tokens;
  public SecurityConfig(TokenStore tokens) { this.tokens = tokens; }

  @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, exception) -> response.sendError(401);
  }

  @Override protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and().exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint())
        .and().authorizeRequests()
          .antMatchers("/api/auth/login").permitAll()
          .antMatchers("/api/auth/admin-test").hasRole("ADMIN")
          .antMatchers("/api/auth/me").authenticated()
          .anyRequest().authenticated()
        .and().addFilterBefore(new BearerTokenFilter(tokens), UsernamePasswordAuthenticationFilter.class);
  }
}
