package com.app4.project.timelapseserver.repository;


import com.app4.project.timelapse.model.User;

import java.util.List;
import java.util.Optional;

public class LocalUserRepository implements UserRepository {

    private final List<User> users;

    public LocalUserRepository(List<User> users) {
        this.users = users;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

}
