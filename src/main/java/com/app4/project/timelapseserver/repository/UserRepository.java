package com.app4.project.timelapseserver.repository;

import com.app4.project.timelapse.model.User;

import java.util.Optional;

public interface UserRepository {
  Optional<User> findByUsername(String username);
}
