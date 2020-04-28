package com.company.security.filters;

import com.sun.net.httpserver.HttpExchange;

public class AuthenticationFilter implements SecurityFilter {
    @Override
    public void execute(HttpExchange request) {
        throw new UnsupportedOperationException();
    }
}
