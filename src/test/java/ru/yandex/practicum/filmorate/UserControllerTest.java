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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void addUser() {
        User user = new User("test@email.com", "testLogin", LocalDate.parse("2000-05-25"));
        user.setName("testUsername");
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        User addedUser = response.getBody();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(addedUser);
        assertEquals(addedUser.getId(), 1);
        assertEquals(addedUser.getEmail(), user.getEmail());
        assertEquals(addedUser.getLogin(), user.getLogin());
        assertEquals(addedUser.getName(), user.getName());
        assertEquals(addedUser.getBirthday(), user.getBirthday());

        User noNameUser = new User("test@email.com", "testLoginAndName", LocalDate.parse("2000-05-25"));
        response = restTemplate.postForEntity("/users", noNameUser, User.class);
        addedUser = response.getBody();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(addedUser);
        assertEquals(addedUser.getId(), 2);
        assertEquals(addedUser.getEmail(), noNameUser.getEmail());
        assertEquals(addedUser.getLogin(), noNameUser.getLogin());
        assertEquals(addedUser.getName(), noNameUser.getLogin());
        assertEquals(addedUser.getBirthday(), noNameUser.getBirthday());

        user = new User("incorrectEmail.com", "testLogin", LocalDate.parse("2000-05-25"));
        response = restTemplate.postForEntity("/users", user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        user = new User("test@email.com", "incorrect login", LocalDate.parse("2000-05-25"));
        response = restTemplate.postForEntity("/users", user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        user = new User("test@email.com", "testLogin", LocalDate.parse("2077-05-25"));
        response = restTemplate.postForEntity("/users", user, User.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateUser() {
        User user = new User("test@email.com", "testLogin", LocalDate.parse("2000-05-25"));
        user.setName("testUsername");
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        User addedUser = response.getBody();
        User newUser = new User("updated@email.com", "updatedLogin", LocalDate.parse("1990-07-14"));
        newUser.setName("updatedUsername");

        assertNotNull(addedUser);

        newUser.setId(addedUser.getId());
        response = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(newUser),
                User.class
        );
        User updatedUser = response.getBody();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(updatedUser);
        assertEquals(updatedUser.getId(), addedUser.getId());
        assertEquals(updatedUser.getEmail(), newUser.getEmail());
        assertEquals(updatedUser.getLogin(), newUser.getLogin());
        assertEquals(updatedUser.getName(), newUser.getName());
        assertEquals(updatedUser.getBirthday(), newUser.getBirthday());

        newUser.setId(999);
        response = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(newUser),
                User.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void getUsers() {
        ResponseEntity<User[]> response = restTemplate.getForEntity("/users", User[].class);
        User[] users = response.getBody();
        User user1 = new User("test@email.com", "testLogin", LocalDate.parse("2000-05-25"));
        user1.setName("testUsername");
        user1.setId(1);
        User user2 = new User("test@email.com", "testLoginAndName", LocalDate.parse("2000-05-25"));
        user2.setName(user2.getLogin());
        user2.setId(2);
        User user3 = new User("updated@email.com", "updatedLogin", LocalDate.parse("1990-07-14"));
        user3.setName("updatedUsername");
        user3.setId(3);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(users);
        assertEquals(users.length, 3);
        assertEquals(users[0], user1);
        assertEquals(users[1], user2);
        assertEquals(users[2], user3);
    }
}
