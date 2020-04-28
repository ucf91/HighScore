package com.company.security.filters;

import com.company.common.exceptions.BadRequestException;
import com.company.infrastructure.Injector;
import com.company.security.repositories.SessionKeysRepo;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import static com.company.common.RestHandler.getQueryParameters;
import static com.company.common.RestHandler.returnResponse;
import static java.time.temporal.ChronoUnit.SECONDS;

public class SessionKeyValidationFilter implements SecurityFilter {
    private static final Logger logger = Logger.getLogger(SessionKeyValidationFilter.class.getName());

    private static final String NOT_AUTHENTICATED_MESSAGE = "You are not Authenticated !!!";
    private static final String INVALID_SESSION_KEY = "Session key is not valid";
    private static final String BAD_REQUEST = "Bad Request";

    private Set<String> authenticatedPathNames = Set.of("/score");
    private final SessionKeysRepo sessionKeysRepo;

    public SessionKeyValidationFilter() {
        sessionKeysRepo = (SessionKeysRepo) Injector.getImplementation(SessionKeysRepo.class);
    }

    @Override
    public void execute(HttpExchange exchange) throws IOException {
        // only request paths which contains any authenticated name will have to use a valid session key
        // I know it's a humble way to do it but for the task it's ok
        boolean authenticatedPath = authenticatedPathNames.stream()
                .anyMatch(authenticatedName -> exchange.getRequestURI().getPath().contains(authenticatedName));
        String queryParams = exchange.getRequestURI().getQuery();
        if (authenticatedPath) {
            if (Objects.isNull(queryParams)) {
                returnResponse(exchange, BAD_REQUEST, 400);
                logger.info(String.format("%s for %s", BAD_REQUEST, exchange.getRequestURI().getPath()));
                throw new BadRequestException(BAD_REQUEST);
            }
            if (Objects.nonNull(queryParams) && queryParams.contains("sessionkey")) {
                String sessionKey = getQueryParameters(exchange.getRequestURI().getQuery()).get("sessionkey");
                if (Objects.isNull(sessionKey) || !isValidKey(sessionKey)) {
                    returnResponse(exchange, NOT_AUTHENTICATED_MESSAGE, 401);
                    logger.info(String.format("%s for %s", INVALID_SESSION_KEY, exchange.getRequestURI().getPath()));
                    throw new BadRequestException(INVALID_SESSION_KEY);
                }
            }
        }
    }

    public boolean isValidKey(String sessionKey) {
        if (!sessionKeysRepo.getSessionKey(sessionKey).isPresent()) {
            return false;
        }
        LocalDateTime expiryDateTime = sessionKeysRepo.getSessionKey(sessionKey)
                .get()
                .getLocalDateTime();

        Duration duration = Duration.between(expiryDateTime, LocalDateTime.now());
        if (duration.get(SECONDS) > 600) {
            sessionKeysRepo.removeKey(sessionKey);
            return false;
        }
        return true;
    }
}
