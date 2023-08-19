package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserStorageTest {
    @Qualifier("databaseUser")
    private final UserStorage storage;

    @Test
    void shouldGetAll() {
        User user1 = new User(1, "test1@email.com", "testLogin1",
                "testName1", LocalDate.parse("2000-05-25"));
        storage.add(user1);
        User user2 = new User(2, "test2@email.com", "testLogin2",
                "testName2", LocalDate.parse("1990-08-12"));
        storage.add(user2);

        List<User> users = storage.getAll();
        assertEquals(2, users.size());
        assertEquals(user1, users.get(0));
        assertEquals(user2, users.get(1));
    }

    @Test
    void shouldAdd() {
        User user = new User(1, "test@email.com", "testLogin",
                "testName", LocalDate.parse("2000-05-25"));
        User addedUser = storage.add(user);

        assertEquals(user, addedUser);
    }

    @Test
    void shouldUpdate() {
        User user = new User(1, "test@email.com", "testLogin",
                "testName", LocalDate.parse("2000-05-25"));
        User addedUser = storage.add(user);
        User newUser = new User(1, "updated@email.com", "updatedLogin",
                "updatedName", LocalDate.parse("1990-07-14"));
        User updatedUser = storage.update(newUser);

        assertEquals(newUser, updatedUser);
    }

    @Test
    void shouldNotUpdateWhenIncorrectId() {
        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> {
                    User user = new User(100, "test@email.com", "testLogin",
                            "testName", LocalDate.parse("2000-05-25"));
                    storage.update(user);
                }
        );

        assertEquals("Пользователь с id=100 не найден.", e.getMessage());
    }

    @Test
    void shouldGetUserById() {
        User user = new User(1, "test@email.com", "testLogin",
                "testName", LocalDate.parse("2000-05-25"));
        User addedUser = storage.add(user);

        User userById = storage.getUserById(user.getId());
        assertEquals(user, userById);
    }

    @Test
    void shouldNotGetUserWhenIncorrectId() {
        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> storage.getUserById(100)
        );

        assertEquals("Пользователь с id=100 не найден.", e.getMessage());
    }
}
