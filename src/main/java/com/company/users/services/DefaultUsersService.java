package com.company.users.services;

import com.company.infrastructure.Injector;
import com.company.users.domains.User;
import com.company.users.repositories.UsersRepo;

public class DefaultUsersService implements UsersService {
    private final UsersRepo usersRepo;

    public DefaultUsersService(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    public DefaultUsersService() {
        this.usersRepo = (UsersRepo) Injector.getImplementation(UsersRepo.class);
    }

    @Override
    public void addUser(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addUserIdIfAbsent(int userId) {
        this.usersRepo.addUser(new User(userId));
    }


}
