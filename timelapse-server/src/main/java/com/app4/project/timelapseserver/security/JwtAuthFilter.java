package com.app4.project.timelapseserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {

  static final String AUTHORIZATION = "Authorization";
  static final String BEARER = "Bearer";

  private final String jwtSecret;

  public JwtAuthFilter(String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, FilterChain filterChain)
      throws ServletException, IOException {
    String header = httpServletRequest.getHeader(AUTHORIZATION);
    //if there is no header -> filter request
    if (header == null || !header.startsWith(BEARER)) {
      filterChain.doFilter(httpServletRequest, httpServletResponse);
      return;
    }
    //extract token
    String token = header.substring(BEARER.length()).trim();

    try {
      Claims claims = Jwts.parser()
          .setSigningKey(jwtSecret)
          .parseClaimsJws(token)
          .getBody();
      String username = claims.getSubject();
      if(username != null) {
        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.get("authorities");

        // 5. Create auth object
        // UsernamePasswordAuthenticationToken: A built-in object, used by spring to represent the current authenticated / being authenticated user.
        // It needs a list of authorities, which has type of GrantedAuthority interface, where SimpleGrantedAuthority is an implementation of that interface
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            username, null, authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

        // 6. Authenticate the user
        // Now, user is authenticated
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (Exception e) {
      SecurityContextHolder.clearContext();
    }
    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }
}
