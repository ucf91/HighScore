package com.company.users.services;

import com.company.users.domains.User;

public interface UsersService {
    void addUser(User user);

    void addUserIdIfAbsent(int userId);
}
