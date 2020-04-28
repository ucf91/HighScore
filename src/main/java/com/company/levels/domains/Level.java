package com.company.levels.domains;

import com.company.scores.domains.Score;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Level {
    private int id;
    // key is user, value a set of user's scores
    // I choosed TreeSet since the number of scores per one user for one level is relatively super small
    // this will maintain the order of the scores so don't need to go through all scores for all users to calculate max score for each user
    // with cost of O(logN) in insertion
    private Map<Integer, TreeSet<Score>> usersScoreList;

    public Level(int id) {
        this.id = id;
        this.usersScoreList = new ConcurrentHashMap<>();
    }

    public int getId() {
        return id;
    }

    public Map<Integer, TreeSet<Score>> getUsersScoreList() {
        return Collections.unmodifiableMap(usersScoreList);
    }

    public void addScore(Score score) {
        usersScoreList.putIfAbsent(score.getUserId(), new TreeSet<>(Comparator.comparing(Score::getScoreValue).reversed()));
        usersScoreList.get(score.getUserId()).add(score);
    }
}
