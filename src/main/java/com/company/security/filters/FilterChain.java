package com.company.security.filters;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface FilterChain {
    void executeFilters(HttpExchange httpExchange) throws IOException;
}
