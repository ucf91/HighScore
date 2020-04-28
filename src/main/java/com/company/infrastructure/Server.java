package com.company.infrastructure;

import java.io.IOException;

public interface Server {
    void start() throws IOException;

    void start(String host, int port) throws IOException;

    void stop();
}
