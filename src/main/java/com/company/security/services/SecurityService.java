package com.company.security.services;

import com.company.security.domains.SessionKey;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Optional;

public interface SecurityService {
    String generateKey(int userId);
    void secure(HttpExchange exchange) throws IOException;
    Optional<SessionKey> getSessionKey(String sessionKey);
}
