package com.company.levels.services;

public interface LevelsService {
    void postLevelScore(int scoreValue, int levelId, int userId);
    String getHighScoreList(int levelId);
}
