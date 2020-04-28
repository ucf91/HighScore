package com.company.users.handlers;

import com.company.BaseTest;
import com.company.infrastructure.DefaultHttpServer;
import com.company.infrastructure.Injector;
import com.company.infrastructure.Server;
import com.company.users.domains.User;
import com.company.users.repositories.UsersRepo;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnitPlatform.class)
public class UsersIntegrationTests extends BaseTest {
    private static Server server;

    static {
        try {
            server = new DefaultHttpServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UsersRepo usersRepo = (UsersRepo) Injector.getImplementation(UsersRepo.class);

    @BeforeAll
    public static void bootServer() throws IOException {
        server.start(BaseTest.HOST,PORT);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("when the user logins providing an userId then a session key should be returned with status 200")
    public void testLogin_happyPath() throws IOException {
        // Given
        HttpUriRequest request = new HttpGet(SERVER_ADDRESS + "/users/1/login");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("200 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));
        String returnedSessionKey = new String(httpResponse.getEntity().getContent().readAllBytes(), UTF_8);
        assertThat("a non empty string should be returned as sessionKey",
                Objects.nonNull(returnedSessionKey) && returnedSessionKey.length() > 0,
                equalTo(true));

    }

    @Test
    @DisplayName("when the user logins providing a non existed userId, then a new user should be created with a new session key returned")
    public void testLogin_newUser() throws IOException {
        // Given
        int userId = 9999;
        HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/users/%d/login", userId));
        assertThat("given userId " + userId + " is not exist", usersRepo.getUser(userId).isEmpty(), equalTo(true));

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("200 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));

        String returnedSessionKey = new String(httpResponse.getEntity().getContent().readAllBytes(), UTF_8);
        assertThat("a non empty string should be returned as sessionKey",
                Objects.nonNull(returnedSessionKey) && returnedSessionKey.length() > 0,
                equalTo(true));
        Optional<User> optionalCreatedUser = usersRepo.getUser(userId);
        assertThat("a new user should be created",
                optionalCreatedUser.isPresent() && optionalCreatedUser.get().getId() == userId,
                equalTo(true));

    }

    @Test
    @DisplayName("when the user logins providing an userId, then the sessionKey string returned should not contain any special character")
    public void testLogin_nonStrangeCharacter() throws IOException {
        // Given
        int userId = 1;
        HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/users/%d/login", userId));

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        String returnedSessionKey = new String(httpResponse.getEntity().getContent().readAllBytes(), UTF_8);

        assertThat("sessionKey should not contain special character",
                containsStrangeCharacter(returnedSessionKey),
                equalTo(false));

    }

    @Test
    @DisplayName("when the user call wrong url then 404 should be returned")
    public void testWrongUrl() throws IOException {
        // Given
        HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/users/5/wrongUrl"));
        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("404 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_NOT_FOUND));
    }

    @Test
    @DisplayName("when the user call login but with non numeric userId value")
    public void testLogin_nonNumericValue() throws IOException {
        // Given
        HttpUriRequest request = new HttpGet(String.format(SERVER_ADDRESS + "/users/WRONGVALUE/login"));
        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertThat("400 status code should be returned",
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_BAD_REQUEST));
    }



    private static boolean containsStrangeCharacter(String sessionKey) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sessionKey);
        return m.find() || sessionKey.contains(" ");
    }
}
