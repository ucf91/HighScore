package com.company.users.handlers;

import com.company.common.RestHandler;
import com.company.common.exceptions.BadRequestException;
import com.company.common.exceptions.NotFoundException;
import com.company.infrastructure.Injector;
import com.company.security.services.SecurityService;
import com.company.users.services.UsersService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.logging.Logger;

public class UsersHandler extends RestHandler {
    private static final Logger logger = Logger.getLogger(UsersHandler.class.getName());

    private final static String CONTEXT_PATH = "/users";

    private final String loginPathPattern;
    private final SecurityService securityService;
    private final UsersService usersService;

    public UsersHandler() {
        this.contextPath = CONTEXT_PATH;
        this.loginPathPattern = CONTEXT_PATH + "/([^/]+)/login";

        this.securityService = (SecurityService) Injector.getImplementation(SecurityService.class);
        this.usersService = (UsersService) Injector.getImplementation(UsersService.class);
    }

    // path : /users/userId/login
    private void login(HttpExchange exchange) throws IOException {
        // since in each path we know exactly which path is a path variable and here the first value will be our userId
        int userId = getUserIdFromRequest(exchange);

        //add the user in case it was not added before
        usersService.addUserIdIfAbsent(userId);

        String generatedKey = securityService.generateKey(userId);

        returnResponse(exchange, generatedKey);
    }

    @Override
    public void process(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod()) && isPathMatches(exchange, loginPathPattern)) {
            login(exchange);
        } else {
            returnResponse(exchange, "", 404);
            throw new NotFoundException(String.format("path %s not found", exchange.getRequestURI().getPath()));
        }
    }

    private int getUserIdFromRequest(HttpExchange exchange) throws IOException {
        try {
            return Integer.parseInt(getPathParameters(exchange.getRequestURI().getPath()).get(0));
        } catch (NumberFormatException ex) {
            String errorMsg = "User id has to be a number";
            returnResponse(exchange, errorMsg, 400);
            logger.info(String.format("%s for request: %s", errorMsg, exchange.getRequestURI().getPath()));
            throw new BadRequestException(errorMsg);
        }
    }


}
