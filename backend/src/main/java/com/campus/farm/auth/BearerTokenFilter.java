package com.campus.farm.auth;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class BearerTokenFilter extends OncePerRequestFilter {
  private final TokenStore tokens;
  public BearerTokenFilter(TokenStore tokens) { this.tokens = tokens; }

  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ") && header.length() > 7) {
      tokens.find(header.substring(7).trim()).ifPresent(user -> {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null,
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
      });
    }
    chain.doFilter(request, response);
  }
}
