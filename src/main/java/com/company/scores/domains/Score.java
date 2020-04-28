package com.company.scores.domains;

import java.time.LocalDateTime;
import java.util.Objects;

public class Score {
    private int scoreValue;
    private int userId;
    private int levelId;
    private LocalDateTime time;

    public Score(int scoreValue, int levelId, int userId) {
        this.scoreValue = scoreValue;
        this.levelId = levelId;
        this.userId = userId;
        this.time = LocalDateTime.now();
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public int getUserId() {
        return userId;
    }

    public int getLevelId() {
        return levelId;
    }

    public LocalDateTime getTime(){
        return time;
    }

    // so if we have two score objects with these same values but different time value
    // then they will be considered equalled and the second score object will not be added
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return scoreValue == score.scoreValue &&
                userId == score.userId &&
                levelId == score.levelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scoreValue, userId, levelId);
    }
}
