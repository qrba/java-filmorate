package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    private final String resource = "/users";

    @Test
    void shouldAddUser() {
        User user = new User(1, "test@email.com", "testLogin",
                "testUsername", LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);
        User addedUser = response.getBody();

        assertNotNull(addedUser);

        user.setId(addedUser.getId());

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(user, addedUser);
    }

    @Test
    void shouldAddUserWhenNoName() {
        User user = new User(1, "test@email.com", "testLogin",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);
        User addedUser = response.getBody();

        assertNotNull(addedUser);

        user.setId(addedUser.getId());
        user.setName(user.getLogin());

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(user, addedUser);
    }

    @Test
    void shouldNotAddUserWhenIncorrectEmail() {
        User user = new User(1, "incorrectEmail.com", "testLogin",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotAddUserWhenIncorrectLogin() {
        User user = new User(1, "test@email.com", "incorrect login",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotAddUserWhenIncorrectBirthday() {
        User user = new User(1, "test@email.com", "testLogin",
                null, LocalDate.parse("2077-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldUpdateUser() {
        User user = new User(1, "test@email.com", "testLogin",
                null, LocalDate.parse("2000-05-25"));
        user.setName("testUsername");
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);
        User addedUser = response.getBody();
        User newUser = new User(1, "updated@email.com", "updatedLogin",
                null, LocalDate.parse("1990-07-14"));
        newUser.setName("updatedUsername");

        assertNotNull(addedUser);

        newUser.setId(addedUser.getId());
        response = restTemplate.exchange(
                resource,
                HttpMethod.PUT,
                new HttpEntity<>(newUser),
                User.class
        );
        User updatedUser = response.getBody();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(newUser, updatedUser);
    }

    @Test
    void shouldNotUpdateUserWhenIncorrectId() {
        User user = new User(1, "test@email.com", "testLogin",
                null, LocalDate.parse("2000-05-25"));
        user.setName("testUsername");
        user.setId(999);
        ResponseEntity<User> response = restTemplate.exchange(
                resource,
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetUsers() {
        User user1 = new User(1, "test1@email.com", "testLogin1",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user1, User.class);
        user1 = response.getBody();
        User user2 = new User(2, "test2@email.com", "testLogin2",
                null, LocalDate.parse("1990-08-12"));
        response = restTemplate.postForEntity(resource, user2, User.class);
        user2 = response.getBody();
        ResponseEntity<User[]> getResponse = restTemplate.getForEntity(resource, User[].class);
        User[] users = getResponse.getBody();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(users);
        assertEquals(2, users.length);
        assertEquals(user1, users[0]);
        assertEquals(user2, users[1]);
    }

    @Test
    void shouldGetUserById() {
        User user = new User(1, "test@email.com", "testLogin",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);
        User addedUser = response.getBody();

        assertNotNull(addedUser);

        ResponseEntity<User> getResponse = restTemplate.getForEntity(resource + "/" + addedUser.getId(), User.class);
        User receivedUser = getResponse.getBody();

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(receivedUser);
        assertEquals(addedUser, receivedUser);
    }

    @Test
    void shouldNotGetUserByIdWhenIncorrectId() {
        ResponseEntity<User> response = restTemplate.getForEntity(resource + "/1", User.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldAddFriend() {
        User user1 = new User(1, "test1@email.com", "testLogin1",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user1, User.class);
        user1 = response.getBody();
        User user2 = new User(2, "test2@email.com", "testLogin2",
                null, LocalDate.parse("1990-06-11"));
        response = restTemplate.postForEntity(resource, user2, User.class);
        user2 = response.getBody();

        assertNotNull(user1);
        assertNotNull(user2);

        ResponseEntity<Void> voidResponse = restTemplate.exchange(
                resource + "/" + user1.getId() + "/friends/" + user2.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );
        ResponseEntity<User> getResponse = restTemplate.getForEntity(resource + "/" + user1.getId(), User.class);
        user1 = getResponse.getBody();
        getResponse = restTemplate.getForEntity(resource + "/" + user2.getId(), User.class);
        user2 = getResponse.getBody();

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());
    }

    @Test
    void shouldNotAddFriendWhenIncorrectId() {
        ResponseEntity<Void> response = restTemplate.exchange(
                resource + "/1/friends/2",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldDeleteFriend() {
        User user1 = new User(1, "test1@email.com", "testLogin1",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user1, User.class);
        user1 = response.getBody();
        User user2 = new User(2, "test2@email.com", "testLogin2",
                null, LocalDate.parse("1990-06-11"));
        response = restTemplate.postForEntity(resource, user2, User.class);
        user2 = response.getBody();

        assertNotNull(user1);
        assertNotNull(user2);

        ResponseEntity<Void> voidResponse = restTemplate.exchange(
                resource + "/" + user1.getId() + "/friends/" + user2.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());

        voidResponse = restTemplate.exchange(
                resource + "/" + user1.getId() + "/friends/" + user2.getId(),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class
        );
        ResponseEntity<User> getResponse = restTemplate.getForEntity(resource + "/" + user1.getId(), User.class);
        user1 = getResponse.getBody();
        getResponse = restTemplate.getForEntity(resource + "/" + user2.getId(), User.class);
        user2 = getResponse.getBody();

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());
        assertNotNull(user1);
        assertNotNull(user2);
        assertTrue(user1.getFriends().isEmpty());
        assertTrue(user2.getFriends().isEmpty());
    }

    @Test
    void shouldNotDeleteFriendWhenIncorrectId() {
        ResponseEntity<Void> response = restTemplate.exchange(
                resource + "/1/friends/2",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Disabled
    @Test
    void shouldGetFriends() {
        User user1 = new User(1, "test1@email.com", "testLogin1",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user1, User.class);
        user1 = response.getBody();
        User user2 = new User(2, "test2@email.com", "testLogin2",
                null, LocalDate.parse("1990-06-11"));
        response = restTemplate.postForEntity(resource, user2, User.class);
        user2 = response.getBody();

        assertNotNull(user1);
        assertNotNull(user2);

        ResponseEntity<Void> voidResponse = restTemplate.exchange(
                resource + "/" + user1.getId() + "/friends/" + user2.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );
        response = restTemplate.getForEntity(resource + "/" + user1.getId(), User.class);
        user1 = response.getBody();
        response = restTemplate.getForEntity(resource + "/" + user2.getId(), User.class);
        user2 = response.getBody();

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());
        assertNotNull(user1);
        assertNotNull(user2);

        ResponseEntity<User[]> getResponse = restTemplate.getForEntity(
                resource + "/" + user1.getId() + "/friends",
                User[].class
        );
        User[] friends = getResponse.getBody();

        assertNotNull(friends);
        assertEquals(1, friends.length);
        assertEquals(user2, friends[0]);

        getResponse = restTemplate.getForEntity(
                resource + "/" + user2.getId() + "/friends",
                User[].class
        );
        friends = getResponse.getBody();

        assertNotNull(friends);
        assertEquals(1, friends.length);
        assertEquals(user1, friends[0]);
    }

    @Test
    void shouldNotGetFriendsWhenIncorrectId() {
        ResponseEntity<Void> response = restTemplate.exchange(
                resource + "/1/friends",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Disabled
    @Test
    void shouldGetCommonFriends() {
        User user1 = new User(1, "test1@email.com", "testLogin1",
                null, LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user1, User.class);
        user1 = response.getBody();
        User user2 = new User(2, "test2@email.com", "testLogin2",
                null, LocalDate.parse("1990-06-11"));
        response = restTemplate.postForEntity(resource, user2, User.class);
        user2 = response.getBody();
        User user3 = new User(3, "test3@email.com", "testLogin3",
                null, LocalDate.parse("1995-08-02"));
        response = restTemplate.postForEntity(resource, user3, User.class);
        user3 = response.getBody();

        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(user3);

        ResponseEntity<Void> voidResponse = restTemplate.exchange(
                resource + "/" + user1.getId() + "/friends/" + user2.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());

        voidResponse = restTemplate.exchange(
                resource + "/" + user1.getId() + "/friends/" + user3.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());

        voidResponse = restTemplate.exchange(
                resource + "/" + user2.getId() + "/friends/" + user3.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());

        response = restTemplate.getForEntity(resource + "/" + user1.getId(), User.class);
        user1 = response.getBody();
        response = restTemplate.getForEntity(resource + "/" + user2.getId(), User.class);
        user2 = response.getBody();
        response = restTemplate.getForEntity(resource + "/" + user3.getId(), User.class);
        user3 = response.getBody();

        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(user3);

        ResponseEntity<User[]> getResponse = restTemplate.getForEntity(
                resource + "/" + user1.getId() + "/friends/common/" + user3.getId(),
                User[].class
        );
        User[] friends = getResponse.getBody();

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(friends);
        assertEquals(1, friends.length);
        assertEquals(user2, friends[0]);
    }

    @Test
    void shouldNotGetCommonFriendsWhenIncorrectId() {
        ResponseEntity<Void> response = restTemplate.exchange(
                resource + "/1/friends/common/2",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
