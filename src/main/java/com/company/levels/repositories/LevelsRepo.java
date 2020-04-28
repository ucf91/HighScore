package com.company.levels.repositories;

import com.company.levels.domains.Level;
import com.company.scores.domains.Score;

import java.util.Optional;

public interface LevelsRepo {
    Optional<Level> getLevel(int levelId);
    void addLevel(Level level);
    boolean addScore(int levelId, Score score);
}
