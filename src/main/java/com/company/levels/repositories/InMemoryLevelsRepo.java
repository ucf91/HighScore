package com.company.levels.repositories;

import com.company.levels.domains.Level;
import com.company.scores.domains.Score;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLevelsRepo implements LevelsRepo {
    private static Map<Integer,Level> levels = new ConcurrentHashMap<>();

    @Override
    public Optional<Level> getLevel(int levelId) {
        return Optional.ofNullable(levels.get(levelId));
    }

    @Override
    public void addLevel(Level level) {
        // important in case we had two threads trying to add same new level
        levels.putIfAbsent(level.getId(),level);
    }

    @Override
    public boolean addScore(int levelId, Score score) {
        if(!levels.containsKey(levelId)){
            return false;
        }
        levels.get(levelId).addScore(score);
        return true;
    }
}
