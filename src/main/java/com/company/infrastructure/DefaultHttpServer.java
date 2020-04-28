package com.company.infrastructure;

import com.company.common.RestHandler;
import com.company.utils.Util;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class DefaultHttpServer implements Server {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private HttpServer httpServer;

    private String host;
    private int port;
    private int backlogSocketConnections;
    private final Properties properties;
    public DefaultHttpServer() throws IOException {
        //default configurations
        this.host = "localhost";
        this.port = 8081;
        this.backlogSocketConnections = 1000;
        properties = Util.getProperties();
    }

    private void createContexts(List<Object> restHandlerList) {
        restHandlerList.stream()
                .map(obj -> (RestHandler) obj)
                .forEach(handler -> httpServer.createContext(handler.getContextPath(), handler));
    }
    private void configureServer() throws IOException {
        assignPropertiesConfiguration();
        InetSocketAddress serverAddress = new InetSocketAddress(host, port);
        createServer(serverAddress);
    }

    private void configureServer(String host,int port) throws IOException {
        InetSocketAddress serverAddress = new InetSocketAddress(host, port);
        createServer(serverAddress);
    }
    private void createServer(InetSocketAddress serverAddress) throws IOException {
        httpServer = HttpServer.create(serverAddress, backlogSocketConnections);
        createContexts(Injector.getImplementations(RestHandler.class));
        httpServer.setExecutor(newCachedThreadPool());
    }


    @Override
    public void start() throws IOException {
        configureServer();
        httpServer.start();
        logger.log(Level.INFO, String.format("Server started successfully on port:%d", port));
    }

    @Override
    public void start(String host, int port) throws IOException {
        configureServer(host,port);
        httpServer.start();
        logger.log(Level.INFO, String.format("Server started successfully on port:%d", port));
    }

    @Override
    public void stop() {
        httpServer.stop(0);
        logger.log(Level.INFO, "Server has been stopped");
    }


    private void assignPropertiesConfiguration() {
        try {
            this.host = properties.getProperty("server.host");
            this.port = Integer.valueOf(properties.getProperty("server.port"));
            this.backlogSocketConnections = Integer.valueOf(properties.getProperty("server.backlog"));
        } catch (Exception e) {
            logger.warning("error loading properties value, fallback to default configuration");
        }
    }
}
