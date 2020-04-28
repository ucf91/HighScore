package com.company.security.repositories;

import com.company.security.domains.SessionKey;

import java.util.Map;
import java.util.Optional;

public interface SessionKeysRepo {
    void addKey(SessionKey sessionKey);
    Optional<SessionKey> getSessionKey(String key);
    Map<String, SessionKey> getKeys();
    void removeKey(String key);
}
