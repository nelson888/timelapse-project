package com.app4.project.timelapseserver.repository;

import com.app4.project.timelapseserver.security.UserDetailsImpl;

import java.util.Optional;

public interface UserRepository {
  Optional<UserDetailsImpl> findByUsername(String username);
}
