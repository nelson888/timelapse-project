package com.app4.project.timelapse.model;

public class AuthResponse {

  private String username;
  private String jwt;


  public AuthResponse(String username, String jwt) {
    this.username = username;
    this.jwt = jwt;
  }

  public AuthResponse() {
  }

  public String getJwt() {
    return jwt;
  }

  public String getUsername() {
    return username;
  }
}
