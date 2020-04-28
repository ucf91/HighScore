package com.company.users.repositories;

import com.company.users.domains.User;

import java.util.Optional;

public interface UsersRepo {
    void addUser(User user);
    Optional<User> getUser(int userId);
}
