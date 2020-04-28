package com.company;

import com.company.infrastructure.DefaultHttpServer;
import com.company.infrastructure.Server;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnitPlatform.class)
public class SystemLoadTests extends BaseTest {
    private static Server server;

    static {
        try {
            server = new DefaultHttpServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    @DisplayName("load test with 1500 users login,post scores and getting high scores, then returned 1500 unique session keys always be unique and all requests/responses to post scores and get highscores should always suceed")
    public void testLogin_uniqueSessionKey() throws InterruptedException, IOException {
        // Given
        Set<Object> returnedSessionKeys = Collections.synchronizedSet(new HashSet<>());

        // When
        ExecutorService es = Executors.newFixedThreadPool(3);
        es.execute(() -> {
            IntStream.range(1, 501).forEach(i -> {
                HttpResponse httpResponse = null;
                try {
                    HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/users/%d/login", i));
                    httpResponse = HttpClientBuilder.create().build().execute(request);
                    String sessionKey = new String(httpResponse.getEntity().getContent().readAllBytes(), UTF_8);
                    returnedSessionKeys.add(sessionKey);
                    int score = 5001 + i;
                    HttpPost postScoreRequest = new HttpPost(String.format(SERVER_ADDRESS + "/levels/1/score?sessionkey=%s", sessionKey));
                    postScoreRequest.setEntity(new StringEntity(String.valueOf(score)));
                    assertThat(HttpClientBuilder.create().build().execute(postScoreRequest).getStatusLine().getStatusCode(), equalTo(200));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        es.execute(() -> {
            IntStream.range(1, 501).forEach(i -> {
                HttpResponse httpResponse = null;
                try {
                    HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/users/%d/login", i));
                    httpResponse = HttpClientBuilder.create().build().execute(request);
                    String sessionKey = new String(httpResponse.getEntity().getContent().readAllBytes(), UTF_8);
                    returnedSessionKeys.add(sessionKey);
                    int score = 5001 + i + 10;
                    HttpPost postScoreRequest = new HttpPost(String.format(SERVER_ADDRESS + "/levels/1/score?sessionkey=%s", sessionKey));
                    postScoreRequest.setEntity(new StringEntity(String.valueOf(score)));
                    assertThat(HttpClientBuilder.create().build().execute(postScoreRequest).getStatusLine().getStatusCode(), equalTo(200));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        });
        es.execute(() -> {
            IntStream.range(1, 501).forEach(i -> {
                HttpResponse httpResponse = null;
                try {
                    HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/users/%d/login", i + 500));
                    httpResponse = HttpClientBuilder.create().build().execute(request);
                    String sessionKey = new String(httpResponse.getEntity().getContent().readAllBytes(), UTF_8);
                    returnedSessionKeys.add(sessionKey);
                    int score = 5001 + i + 9;
                    HttpPost postScoreRequest = new HttpPost(String.format(SERVER_ADDRESS + "/levels/1/score?sessionkey=%s", sessionKey));
                    postScoreRequest.setEntity(new StringEntity(String.valueOf(score)));
                    assertThat(HttpClientBuilder.create().build().execute(postScoreRequest).getStatusLine().getStatusCode(), equalTo(200));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        });
        es.shutdown();
        es.awaitTermination(1, TimeUnit.MINUTES);


        HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/levels/%d/highscorelist", 1));

        // since getting highscore list is relatively complex operation we have to make sure that on big loads server can
        // compute the highscores quickly no failures
        IntStream.range(0, 3000).forEach(i -> {
            HttpResponse httpResponse = null;
            try {
                httpResponse = HttpClientBuilder.create().build().execute(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertThat("200 status code should be returned",
                    httpResponse.getStatusLine().getStatusCode(),
                    equalTo(HttpStatus.SC_OK));
        });


        // Then
        assertThat("we should have total of 1500 unique keys",
                returnedSessionKeys.size(),
                equalTo(1500));

        HttpResponse finalHttpResponse = HttpClientBuilder.create().build().execute(request);
        String highScoreResult = new String(finalHttpResponse.getEntity().getContent().readAllBytes(), UTF_8);

        assertThat("a non empty string should be returned", Objects.nonNull(highScoreResult), equalTo(true));
        assertThat("max number of 15 score results returned", highScoreResult.split(",").length <= 15, equalTo(true));

        long numberOfWinnerUsers = Arrays.stream(highScoreResult.split(",")).mapToInt(str -> Integer.valueOf(str.split("=")[0])).distinct().count();
        assertThat("should return 15 unique winner users ids", numberOfWinnerUsers, equalTo(15l));


    }
}
