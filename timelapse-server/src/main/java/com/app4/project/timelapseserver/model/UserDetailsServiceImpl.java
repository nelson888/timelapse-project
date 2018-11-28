package com.app4.project.timelapseserver.model;

import com.app4.project.timelapse.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final Collection<User> users;

  public UserDetailsServiceImpl(
      Collection<User> users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    for (User user : users) {
      if (user.getUsername().equals(username)) {
        return new org.springframework.security.core.userdetails.User(username, user.getPassword(),
            Collections.singleton(new Authority(user.getRole())));
      }
    }
    throw new UsernameNotFoundException("Username: " + username + " not found");
  }
}
