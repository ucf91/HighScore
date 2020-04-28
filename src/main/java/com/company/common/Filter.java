package com.company.common;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface Filter {
    void execute(HttpExchange request) throws IOException;
}
