package com.company;

import com.company.infrastructure.Server;
import com.company.infrastructure.Injector;

import java.io.IOException;

public class Application {

    public static void main(String[] args) throws IOException {
       Server server = (Server) Injector.getImplementation(Server.class);
       server.start();
    }
}
