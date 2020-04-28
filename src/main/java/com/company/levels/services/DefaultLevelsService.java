package com.company.levels.services;

import com.company.infrastructure.Injector;
import com.company.levels.domains.Level;
import com.company.levels.repositories.LevelsRepo;
import com.company.scores.domains.Score;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultLevelsService implements LevelsService {
    private final LevelsRepo levelsRepo;

    public DefaultLevelsService() {
        this.levelsRepo = (LevelsRepo) Injector.getImplementation(LevelsRepo.class);
    }

    @Override
    public void postLevelScore(int scoreValue, int levelId, int userId) {
        Score score = new Score(scoreValue, levelId, userId);
        //no need to synchronize this part of adding new level since we are using more fine grained locking mechanism
        // by using concurrentHashMap for storage and using putIfAbsent so levels will not be overwritten
        Optional<Level> levelOptional = levelsRepo.getLevel(levelId);
        if (levelOptional.isEmpty()) {
            levelsRepo.addLevel(new Level(levelId));
        }
        levelsRepo.addScore(levelId, score);
    }

    @Override
    public String getHighScoreList(int levelId) {
        Optional<Level> levelOptional = levelsRepo.getLevel(levelId);
        if (levelOptional.isEmpty()) {
            return "";
        }

        return levelOptional.get().getUsersScoreList().entrySet().parallelStream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> entry.getValue().first()) // map to max scores for users
                .sorted(Comparator.comparing(Score::getScoreValue).reversed()
                        .thenComparing(Score::getTime)) //in case we got two same high scores the priority will be for the earliest score time
                .limit(15)
                .map(score -> String.format("%s=%s", score.getUserId(), score.getScoreValue()))
                .collect(Collectors.joining(","));
    }
}
