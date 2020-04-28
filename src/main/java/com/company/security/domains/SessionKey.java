package com.company.security.domains;

import java.time.LocalDateTime;

public class SessionKey {
    private String key;
    private int userId;
    private LocalDateTime localDateTime;

    public SessionKey(String key, int userId, LocalDateTime localDateTime) {
        this.key = key;
        this.userId = userId;
        this.localDateTime = localDateTime;
    }

    public String getKey() {
        return key;
    }

    public int getUserId() {
        return userId;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }
}
