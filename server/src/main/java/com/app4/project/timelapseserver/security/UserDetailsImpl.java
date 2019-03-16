package com.app4.project.timelapseserver.security;

import com.app4.project.timelapse.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UserDetailsImpl extends User implements UserDetails {

  //CAREFUL ROLES MUST START WITH THE PREFIX 'ROLE_' BUT NOT IN SECURITY CONFIGURATION
  private List<String> roles;

  public UserDetailsImpl(String username, String password, Role... roles) {
    super(username, password);
    this.roles = Arrays.asList(roles).stream().map(Role::roleName)
      .collect(toList());
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
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.roles.stream().map(SimpleGrantedAuthority::new).collect(toList());
  }

  public List<String> getRoles() {
    return roles;
  }
}
