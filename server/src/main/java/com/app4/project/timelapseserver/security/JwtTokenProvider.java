package com.app4.project.timelapseserver.security;

import com.app4.project.timelapseserver.exception.InvalidJwtAuthenticationException;
import com.app4.project.timelapseserver.repository.UserRepository;
import io.jsonwebtoken.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

//TUTORIEL: https://www.codementor.io/hantsy/protect-rest-apis-with-spring-security-and-jwt-ms5uu3zd6
@Component
public class JwtTokenProvider {

  private static final long VALIDITY_IN_MILLISECONDS = TimeUnit.DAYS.toMillis(30); // infinite validity

  private final String secretKey;
  private final UserRepository userRepository;

  public JwtTokenProvider(String secretKey, UserRepository userRepository) {
    this.secretKey = secretKey;
    this.userRepository = userRepository;
  }

  public String createToken(String username, List<String> roles) {

    Claims claims = Jwts.claims().setSubject(username);
    claims.put("roles", roles);

    Date now = new Date();
    Date validity = new Date(now.getTime() + VALIDITY_IN_MILLISECONDS);

    return Jwts.builder()//
      .setClaims(claims)//
      .setIssuedAt(now)//
      .setExpiration(validity)//
      .signWith(SignatureAlgorithm.HS256, secretKey)//
      .compact();
  }

  public Authentication getAuthentication(String token) {
    String username = getUsername(token);
    UserDetails userDetails = this.userRepository.findByUsername(username)
      .orElseThrow(() -> new BadCredentialsException("User with username " + username + " dosn't exists"));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getUsername(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
  }

  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  public boolean validateToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

      return !claims.getBody().getExpiration().before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidJwtAuthenticationException("Expired or invalid JWT token", e); //TODO unhandled exception handler
    }
  }

}
