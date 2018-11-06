package com.app4.project.timelapseserver.repository;

import com.app4.project.timelapseserver.model.User;

import java.util.Optional;

//TODO to implement
public interface UserRepository {
  Optional<User> findByUsernameOrEmail(String username, String email);
  Optional<User> findById(Long id);
}
