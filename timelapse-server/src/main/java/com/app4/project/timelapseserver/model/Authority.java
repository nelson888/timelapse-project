package com.app4.project.timelapseserver.model;

import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

public class Authority implements GrantedAuthority {

  private final String authority;

  public Authority(String authority) {
    this.authority = authority;
  }

  @Override
  public String getAuthority() {
    return authority;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Authority authority1 = (Authority) o;
    return Objects.equals(authority, authority1.authority);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authority);
  }
}
