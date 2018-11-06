package com.app4.project.timelapseserver.repository;

import com.app4.project.timelapseserver.model.User;

import java.util.List;
import java.util.Optional;

public class LocalUserRepository implements UserRepository {

    private final List<User> users;

    public LocalUserRepository(List<User> users) {
        this.users = users;
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String username, String email) { //TODO no need of email
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }
}
