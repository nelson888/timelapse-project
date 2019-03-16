package com.app4.project.timelapseserver.security;

public enum Role {
  ANDROID, TIMELAPSE, ADMIN;


  public String roleName() {
    return "ROLE_" + name();
  }
}
