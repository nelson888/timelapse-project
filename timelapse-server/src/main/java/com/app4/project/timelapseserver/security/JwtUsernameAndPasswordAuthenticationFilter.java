package com.app4.project.timelapseserver.security;

import com.app4.project.timelapse.model.User;
import com.app4.project.timelapseserver.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import java.util.stream.Collectors;

public class JwtUsernameAndPasswordAuthenticationFilter extends
    UsernamePasswordAuthenticationFilter {
  private static final Logger LOOGER =
      LoggerFactory.getLogger(JwtUsernameAndPasswordAuthenticationFilter.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  // We use auth manager to validate the user credentials
  private final AuthenticationManager authManager;
  private final String jwtSecret;
  private final long expirationTimeInMillis;

  public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authManager,
      String jwtSecret, long expirationTimeInMillis) {
    this.authManager = authManager;
    this.jwtSecret = jwtSecret;
    this.expirationTimeInMillis = expirationTimeInMillis;
    //path and method to authenticate
    this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/auth", "POST"));

  }

  /**
   *
   * 401 -> wrong credentials
   *  -> good credentials and token in header Authorization
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException {

    UserCredentials credentials;
    try {
      // 1. Get credentials from request
      JsonNode jsonNode = OBJECT_MAPPER.readTree(request.getInputStream());
      credentials = new UserCredentials(jsonNode.get("username").asText(),
          jsonNode.get("password").asText());
    } catch (IOException e) {
      throw new MalformedAuthentication("Credentials were malformed", e);
    }
    // 2. Create auth object (contains credentials) which will be used by auth manager
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        credentials.getUsername(), credentials.getPassword(), Collections.emptyList());

    // 3. Authentication manager authenticate the user
    return authManager.authenticate(authToken);
  }

  private class MalformedAuthentication extends AuthenticationException {

    MalformedAuthentication(String msg, Throwable t) {
      super(msg, t);
    }

  }
  // Upon successful authentication, generate a token.
  // The 'auth' passed to successfulAuthentication() is the current authenticated user.
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication auth) throws IOException, ServletException {

    Long now = System.currentTimeMillis();
    String token = Jwts.builder()
        .setSubject(auth.getName())
        // Convert to list of strings.
        // This is important because it affects the way we get them back in the Gateway.
        .claim("authorities", auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + expirationTimeInMillis * 1000))
        .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes())
        .compact();

    // Add token to header
    response.addHeader(JwtAuthFilter.AUTHORIZATION, JwtAuthFilter.BEARER + " " + token);
  }
}
