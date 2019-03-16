package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.AuthResponse;
import com.app4.project.timelapse.model.User;
import com.app4.project.timelapseserver.repository.UserRepository;
import com.app4.project.timelapseserver.security.JwtTokenProvider;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;

  private final JwtTokenProvider jwtTokenProvider;

  private final UserRepository users;

  public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRepository users) {
    this.authenticationManager = authenticationManager;
    this.jwtTokenProvider = jwtTokenProvider;
    this.users = users;
  }

  @PostMapping("/signin")
  public ResponseEntity signin(@RequestBody User data) {
    try {
      String username = data.getUsername();
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
      String token = jwtTokenProvider.createToken(username, this.users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username " + username + "not found")).getRoles());
      return ResponseEntity.ok(new AuthResponse(username, token));
    } catch (AuthenticationException e) {
      throw new BadCredentialsException("Invalid username/password supplied");
    }
  }
}