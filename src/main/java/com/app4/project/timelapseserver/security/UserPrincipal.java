package com.app4.project.timelapseserver.security;

import com.app4.project.timelapseserver.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserPrincipal implements UserDetails {

  private static final List<GrantedAuthority> AUTHORITIES = Collections.singletonList(
      (GrantedAuthority) () -> "ALL"
  );
  private Long id;

  private String username;

  @JsonIgnore
  private String password;

  public UserPrincipal(Long id, String username, String password) {
    this.id = id;
    this.username = username;
    this.password = password;
  }


  public static UserPrincipal create(User user) {
    return new UserPrincipal(
        user.getId(),
        user.getUsername(),
        user.getPassword());
  }

  public Long getId() {
    return id;
  }
  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return AUTHORITIES;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserPrincipal that = (UserPrincipal) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}