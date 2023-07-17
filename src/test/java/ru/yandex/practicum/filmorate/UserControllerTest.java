package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
        User user = new User("test@email.com", "testLogin", LocalDate.parse("2000-05-25"));
        user.setName("testUsername");
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);
        User addedUser = response.getBody();

        assertNotNull(addedUser);

        user.setId(addedUser.getId());

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(user, addedUser);
    }

    @Test
    void shouldAddUserWhenNoName() {
        User user = new User("test@email.com", "testLoginAndName", LocalDate.parse("2000-05-25"));
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
        User user = new User("incorrectEmail.com", "testLogin", LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotAddUserWhenIncorrectLogin() {
        User user = new User("test@email.com", "incorrect login", LocalDate.parse("2000-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotAddUserWhenIncorrectBirthday() {
        User user = new User("test@email.com", "testLogin", LocalDate.parse("2077-05-25"));
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldUpdateUser() {
        User user = new User("test@email.com", "testLogin", LocalDate.parse("2000-05-25"));
        user.setName("testUsername");
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user, User.class);
        User addedUser = response.getBody();
        User newUser = new User("updated@email.com", "updatedLogin", LocalDate.parse("1990-07-14"));
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
        User user = new User("test@email.com", "testLogin", LocalDate.parse("2000-05-25"));
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
        User user1 = new User("test1@email.com", "testLogin1", LocalDate.parse("2000-05-25"));
        user1.setName("testUsername1");
        ResponseEntity<User> response = restTemplate.postForEntity(resource, user1, User.class);
        user1 = response.getBody();
        User user2 = new User("test2@email.com", "testLogin2", LocalDate.parse("1990-08-12"));
        user2.setName("testUsername2");
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
}
