package com.app4.project.timelapseserver.repository;


import com.app4.project.timelapseserver.security.UserDetailsImpl;

import java.util.List;
import java.util.Optional;

public class LocalUserRepository implements UserRepository {

    private final List<UserDetailsImpl> users;

    public LocalUserRepository(List<UserDetailsImpl> users) {
        this.users = users;
    }

    @Override
    public Optional<UserDetailsImpl> findByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

}
