package com.company.security.repositories;

import com.company.security.domains.SessionKey;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionKeyRepo implements SessionKeysRepo {
    private static Map<String, SessionKey> keys = new ConcurrentHashMap<>();

    @Override
    public void addKey(SessionKey sessionKey) {
        keys.put(sessionKey.getKey(), sessionKey);
    }

    @Override
    public Optional<SessionKey> getSessionKey(String key) {
        return Optional.ofNullable(keys.get(key));
    }

    @Override
    public Map<String, SessionKey> getKeys() {
        return Collections.unmodifiableMap(keys);
    }

    @Override
    public void removeKey(String key) {
        keys.remove(key);
    }
}
