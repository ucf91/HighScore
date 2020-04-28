package com.company.security.services;

import com.company.infrastructure.Injector;
import com.company.security.domains.SessionKey;
import com.company.security.filters.FilterChain;
import com.company.security.repositories.SessionKeysRepo;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.IntStream;

public class DefaultSecurityService implements SecurityService {
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final FilterChain filterChain;
    private final SessionKeysRepo sessionKeysRepo;
    private final Object lock = new Object();

    public DefaultSecurityService() {
        this.filterChain = (FilterChain) Injector.getImplementation(FilterChain.class);
        this.sessionKeysRepo = (SessionKeysRepo) Injector.getImplementation(SessionKeysRepo.class);
    }


    @Override
    public String generateKey(int userId) {
        String generatedKey = randomString(String.valueOf(userId).length() + 3);
        //in worst case scenario where you have non unique keys
        //we have to regenerate again
        // also we need to synchronize this operation to make it done by only one thread just in case multiple threads
        // generated the same key which would be a problem because it might end up with different users having the same key
        // one generated the key and the other generated the same one and updated the dateTime
        synchronized (lock) {
            while (sessionKeysRepo.getKeys().containsKey(generatedKey)) {
                generatedKey = randomString(String.valueOf(userId).length() + 3);
            }
            sessionKeysRepo.addKey(new SessionKey(generatedKey, userId, LocalDateTime.now()));
        }
        return generatedKey;
    }

    @Override
    public void secure(HttpExchange exchange) throws IOException {
        this.filterChain.executeFilters(exchange);
    }

    @Override
    public Optional<SessionKey> getSessionKey(String sessionKey) {
        return sessionKeysRepo.getSessionKey(sessionKey);
    }

    private static String randomString(int len) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        IntStream.range(0, len)
                .forEach(i -> sb.append(ALLOWED_CHARACTERS.charAt(rnd.nextInt(ALLOWED_CHARACTERS.length()))));
        return sb.toString();
    }

}
