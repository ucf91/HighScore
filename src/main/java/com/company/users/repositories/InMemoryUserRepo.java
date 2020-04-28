package com.company.users.repositories;

import com.company.users.domains.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepo implements UsersRepo {
    private static Map<Integer, User> users = new ConcurrentHashMap<>();


    @Override
    public void addUser(User user) {
        users.putIfAbsent(user.getId(), user);
    }

    @Override
    public Optional<User> getUser(int userId) {
        return Optional.ofNullable(users.get(userId));
    }
}
