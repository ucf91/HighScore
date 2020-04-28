package com.company.levels;

import com.company.BaseTest;
import com.company.infrastructure.DefaultHttpServer;
import com.company.infrastructure.Injector;
import com.company.infrastructure.Server;
import com.company.levels.domains.Level;
import com.company.levels.repositories.LevelsRepo;
import com.company.scores.domains.Score;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnitPlatform.class)
public class LevelsIntegrationTests extends BaseTest {
    private static Server server;

    static {
        try {
            server = new DefaultHttpServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final String HOST = "http://localhost:8081";

    private LevelsRepo levelsRepo;

    public LevelsIntegrationTests() {
        levelsRepo = (LevelsRepo) Injector.getImplementation(LevelsRepo.class);
    }

    @BeforeAll
    public static void bootServer() throws IOException {
        server.start(BaseTest.HOST,PORT);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("when a loggedin user post a score, then 200 status code should be returned")
    public void testPostScore_happyPath() throws IOException {
        // Given
        int userId = 1;
        int levelId = 5;
        int score = 500;
        HttpUriRequest loginRequest = new HttpGet(String.format(SERVER_ADDRESS + "/users/%d/login", userId));
        HttpResponse loginResponse = HttpClientBuilder.create().build().execute(loginRequest);
        String loginSessionKey = new String(loginResponse.getEntity().getContent().readAllBytes(), UTF_8);
        assertThat("given a valid sessionKey", (Objects.nonNull(loginSessionKey) && loginSessionKey.length() > 0), equalTo(true));
        HttpPost request = new HttpPost(String.format(SERVER_ADDRESS + "/levels/%d/score?sessionkey=%s", levelId, loginSessionKey));
        request.setEntity(new StringEntity(String.valueOf(score)));

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("200 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));


    }

    @Test
    @DisplayName("when a loggedin user post a score without a sessionkey, then 400 status code should be returned")
    public void testPostScore_noSessionKey() throws IOException {
        // Given
        int levelId = 1;
        int score = 500;
        HttpPost request = new HttpPost(String.format(SERVER_ADDRESS + "/levels/%d/score", levelId));
        request.setEntity(new StringEntity(String.valueOf(score)));

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("400 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_BAD_REQUEST));

    }

    @Test
    @DisplayName("when a loggedin user post a score without a non numeric levelId, then 400 status code should be returned")
    public void testPostScore_nonNumericLevelId() throws IOException {
        // Given
        int userId = 1;
        int score = 500;
        HttpUriRequest loginRequest = new HttpGet(String.format(SERVER_ADDRESS + "/users/%d/login", userId));
        HttpResponse loginResponse = HttpClientBuilder.create().build().execute(loginRequest);
        String loginSessionKey = new String(loginResponse.getEntity().getContent().readAllBytes(), UTF_8);
        assertThat("given a valid sessionKey", (Objects.nonNull(loginSessionKey) && loginSessionKey.length() > 0), equalTo(true));
        HttpPost request = new HttpPost(String.format(SERVER_ADDRESS + "/levels/WRONgValue/score?sessionkey=%s", loginSessionKey));
        request.setEntity(new StringEntity(String.valueOf(score)));

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("400 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_BAD_REQUEST));

    }

    @Test
    @DisplayName("when a loggedin user post a score for a non existent level id, then 200 status code should be returned and new level will be created")
    public void testPostScore_nonExistentLevelId() throws IOException {
        // Given
        int userId = 1;
        int levelId = 5;
        int score = 500;
        HttpUriRequest loginRequest = new HttpGet(String.format(SERVER_ADDRESS + "/users/%d/login", userId));
        HttpResponse loginResponse = HttpClientBuilder.create().build().execute(loginRequest);
        String loginSessionKey = new String(loginResponse.getEntity().getContent().readAllBytes(), UTF_8);
        assertThat("given a valid sessionKey", (Objects.nonNull(loginSessionKey) && loginSessionKey.length() > 0), equalTo(true));
        Optional<Level> levelOpt = levelsRepo.getLevel(levelId);
        assertThat("Given no level stored for this id", levelOpt.isEmpty(), equalTo(true));

        HttpPost request = new HttpPost(String.format(SERVER_ADDRESS + "/levels/%d/score?sessionkey=%s", levelId, loginSessionKey));
        request.setEntity(new StringEntity(String.valueOf(score)));


        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("200 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));
        levelOpt = levelsRepo.getLevel(levelId);
        assertThat("a new level should be added", (levelOpt.isPresent() &&
                levelOpt.get().getId() == levelId &&
                levelOpt.get().getUsersScoreList().size() > 0), equalTo(true));

    }

    @Test
    @DisplayName("when an user request highscore for an empty level then 200 status code returned with empty string")
    public void testGetHighScore_noData() throws IOException {
        int levelId = 99;
        // Given
        HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/levels/%d/highscorelist", levelId));

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("200 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));
        String result = new String(httpResponse.getEntity().getContent().readAllBytes(), UTF_8);
        assertThat("an empty string should be returned", Objects.nonNull(result) && result.length() == 0,
                equalTo(true));

    }

    @Test
    @DisplayName("when an user request highscore ,then 200 status code returned with high score list")
    public void testGetHighScore_happyPath() throws IOException {
        int levelId = 1;
        // Given
        Level level = new Level(levelId);
        levelsRepo.addLevel(level);
        // every user have two scores and every one appear only once
        IntStream.range(1, 21)
                .forEach(i -> {
                    levelsRepo.addScore(1, new Score(500, 1, i));
                    levelsRepo.addScore(1, new Score(500 + i, 1, i));
                });
        int maxScore = 520;
        int minScore = 506;

        HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/levels/%d/highscorelist", levelId));

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("200 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));

        String result = new String(httpResponse.getEntity().getContent().readAllBytes(), UTF_8);
        assertThat("a non empty string should be returned", Objects.nonNull(result), equalTo(true));
        assertThat("max number of 15 score results returned", result.split(",").length <= 15, equalTo(true));

        int returnedMaxScore = Integer.valueOf(result.split(",")[0].split("=")[1]);
        int returnedMinScore = Integer.valueOf(result.split(",")[14].split("=")[1]);
        assertThat("first score should be the max result", returnedMaxScore, equalTo(maxScore));
        assertThat("last 15th score should be the min result", returnedMinScore, equalTo(minScore));

        long numberOfWinnerUsers = Arrays.stream(result.split(",")).mapToInt(str -> Integer.valueOf(str.split("=")[0])).distinct().count();
        assertThat("should return 15 unique winner users ids", numberOfWinnerUsers, equalTo(15l));
    }
}
