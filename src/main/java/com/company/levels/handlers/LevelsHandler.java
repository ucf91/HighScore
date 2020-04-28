package com.company.levels.handlers;

import com.company.common.RestHandler;
import com.company.common.exceptions.BadRequestException;
import com.company.common.exceptions.InternalServerException;
import com.company.common.exceptions.NotFoundException;
import com.company.infrastructure.Injector;
import com.company.levels.services.LevelsService;
import com.company.security.domains.SessionKey;
import com.company.security.services.SecurityService;
import com.sun.net.httpserver.HttpExchange;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LevelsHandler extends RestHandler {
    private static final Logger logger = Logger.getLogger(LevelsHandler.class.getName());

    private static final String CONTEXT_PATH = "/levels";
    private final String postScorePathPattern;
    private final String getHighScorePathPattern;

    private final LevelsService levelsService;
    private final SecurityService securityService;

    public LevelsHandler() {
        this.contextPath = CONTEXT_PATH;
        this.postScorePathPattern = CONTEXT_PATH + "/([^/]+)/score";
        this.getHighScorePathPattern = CONTEXT_PATH + "/([^/]+)/highscorelist";

        this.levelsService = (LevelsService) Injector.getImplementation(LevelsService.class);
        this.securityService = (SecurityService) Injector.getImplementation(SecurityService.class);
    }

    private void postScore(HttpExchange exchange) throws IOException {
        int levelId = getLevelIdFromRequest(exchange);

        int userId = getUserIdFromSessionKey(exchange);

        int scoreValue = getScoreValueFromRequest(exchange);

        levelsService.postLevelScore(scoreValue, levelId, userId);

        returnResponse(exchange, "", 200);
    }

    private void getHighScoreListForLevel(HttpExchange exchange) throws IOException {
        int levelId = getLevelIdFromRequest(exchange);
        returnResponse(exchange, levelsService.getHighScoreList(levelId), 200);
    }


    @Override
    public void process(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod()) && isPathMatches(exchange, postScorePathPattern)) {
            postScore(exchange);
        } else if ("GET".equals(exchange.getRequestMethod()) && isPathMatches(exchange, getHighScorePathPattern)) {
            getHighScoreListForLevel(exchange);
        } else {
            returnResponse(exchange, "", 404);
            throw new NotFoundException(String.format("path %s not found", exchange.getRequestURI().getPath()));
        }
    }

    private int getLevelIdFromRequest(HttpExchange exchange) throws IOException {
        try {
            return Integer.parseInt(getPathParameters(exchange.getRequestURI().getPath()).get(0));
        } catch (NumberFormatException ex) {
            String errorMsg = "Level id has to be a number";
            returnResponse(exchange, errorMsg, 400);
            logger.info(errorMsg);
            throw new BadRequestException(errorMsg);
        }
    }

    private String getSessionKeyFromRequest(HttpExchange exchange) throws IOException {
        String sessionKey = getQueryParameters(exchange.getRequestURI().getQuery()).get("sessionkey");
        if (Objects.isNull(sessionKey)) {
            String errorMsg = "session key has to be provided";
            returnResponse(exchange, errorMsg, 400);
            logger.info(errorMsg);
            throw new BadRequestException(errorMsg);
        }
        return sessionKey;
    }

    private int getScoreValueFromRequest(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        DataInputStream dis = new DataInputStream(inputStream);
        String requestBody = new String(inputStream.readAllBytes(), UTF_8);
        dis.close();
        int score = 0;
        try {
            score = Integer.parseInt(requestBody);
        } catch (NumberFormatException ex) {
            String errorMsg = "score value has to be a number";
            returnResponse(exchange, errorMsg, 400);
            logger.info(errorMsg);
            throw new BadRequestException(errorMsg);
        }
        return score;
    }

    private int getUserIdFromSessionKey(HttpExchange exchange) throws IOException {
        Optional<SessionKey> sessionKey = securityService.getSessionKey(getSessionKeyFromRequest(exchange));
        if (!sessionKey.isPresent()) {
            String errorMsg = "Internal server Error";
            returnResponse(exchange, errorMsg, 500);
            logger.warning(String.format("%s sessionkey not found in database", errorMsg));
            throw new InternalServerException(errorMsg);
        }
        return sessionKey.get().getUserId();
    }
}
