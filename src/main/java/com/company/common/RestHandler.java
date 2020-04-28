package com.company.common;

import com.company.infrastructure.Injector;
import com.company.security.services.SecurityService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class RestHandler implements HttpHandler {
    private static Logger logger = Logger.getLogger(RestHandler.class.getName());

    private final SecurityService securityService;
    private Set<String> pathNames = Set.of("users", "levels", "highscorelist", "score", "login");

    public RestHandler() {
        this.securityService = (SecurityService) Injector.getImplementation(SecurityService.class);
    }

    protected String contextPath;

    @Override
    public void handle(HttpExchange exchange) {
        try {
            securityService.secure(exchange);
            process(exchange);
        } catch (Exception ex) {
            logger.log(Level.INFO, ex.getMessage());
        }finally {
            exchange.close();
        }
    }

    public abstract void process(HttpExchange exchange) throws IOException;

    protected List<String> getPathParameters(String path) {
        //extract path values list among named defined pathNames
        return Arrays.stream(path.split("/"))
                .filter(pathVal -> !pathVal.isEmpty() && !pathNames.contains(pathVal))
                .collect(Collectors.toList());
    }

    public static Map<String, String> getQueryParameters(String queryParamsString) {
        return Arrays.stream(queryParamsString.split("&"))
                .filter(query -> query.length() > 1)
                .map(query -> query.split("="))
                .filter(pairKeyVal -> pairKeyVal.length == 2)
                .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));
    }

    public static void returnResponse(HttpExchange exchange, String response, int httpStatusCode) throws IOException {
        exchange.sendResponseHeaders(httpStatusCode, response.length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    protected static void returnResponse(HttpExchange exchange, String response) throws IOException {
        returnResponse(exchange, response, 200);
    }

    public String getContextPath() {
        return contextPath;
    }

    protected static boolean isPathMatches(HttpExchange exchange, String pathPattern) {
        return exchange.getRequestURI().getPath().matches(pathPattern);
    }
}
