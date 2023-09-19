package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserStorage storage;

    @Test
    void shouldGetAll() {
        User userToAdd = User.builder()
                .email("test1@email.com")
                .login("testLogin1")
                .name("testUsername1")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User user1 = storage.add(userToAdd);
        userToAdd = User.builder()
                .email("test2@email.com")
                .login("testLogin2")
                .name("testUsername2")
                .birthday(LocalDate.parse("1987-01-10"))
                .build();
        User user2 = storage.add(userToAdd);
        List<User> users = storage.getAll();

        assertEquals(2, users.size());
        assertEquals(user1, users.get(0));
        assertEquals(user2, users.get(1));
    }

    @Test
    void shouldAdd() {
        User user = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("testUsername")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User addedUser = storage.add(user);
        user.setId(addedUser.getId());

        assertEquals(user, addedUser);
    }

    @Test
    void shouldUpdate() {
        User userToAdd = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("testUsername")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User user = storage.add(userToAdd);
        userToAdd = User.builder()
                .id(user.getId())
                .email("updated@email.com")
                .login("updatedLogin")
                .name("updatedTestUsername")
                .birthday(LocalDate.parse("2001-05-25"))
                .build();
        User updatedUser = storage.update(userToAdd);

        assertEquals(userToAdd, updatedUser);
    }

    @Test
    void shouldNotUpdateWhenIncorrectId() {
        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> {
                    User user = User.builder()
                            .id(100)
                            .email("test@email.com")
                            .login("testLogin")
                            .name("testUsername")
                            .birthday(LocalDate.parse("2000-05-25"))
                            .build();
                    storage.update(user);
                }
        );

        assertEquals("Пользователь с id 100 не существует", e.getMessage());
    }

    @Test
    void shouldGetUserById() {
        User userToAdd = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("testUsername")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User user = storage.add(userToAdd);
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

    @Test
    void shouldAddDeleteGetFriend() {
        User userToAdd = User.builder()
                .email("test1@email.com")
                .login("testLogin1")
                .name("testUsername1")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User user1 = storage.add(userToAdd);
        userToAdd = User.builder()
                .email("test2@email.com")
                .login("testLogin2")
                .name("testUsername2")
                .birthday(LocalDate.parse("1987-01-10"))
                .build();
        User user2 = storage.add(userToAdd);
        storage.addFriend(user1.getId(), user2.getId());
        List<User> friends = storage.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertEquals(user2, friends.get(0));
    }

    @Test
    void shouldGetCommonFriends() {
        User userToAdd = User.builder()
                .email("test1@email.com")
                .login("testLogin1")
                .name("testUsername1")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User user1 = storage.add(userToAdd);
        userToAdd = User.builder()
                .email("test2@email.com")
                .login("testLogin2")
                .name("testUsername2")
                .birthday(LocalDate.parse("1987-01-10"))
                .build();
        User user2 = storage.add(userToAdd);
        userToAdd = User.builder()
                .email("test3@email.com")
                .login("testLogin3")
                .name("testUsername3")
                .birthday(LocalDate.parse("2010-12-12"))
                .build();
        User user3 = storage.add(userToAdd);
        storage.addFriend(user1.getId(), user2.getId());
        storage.addFriend(user1.getId(), user3.getId());
        storage.addFriend(user2.getId(), user3.getId());
        List<User> commonFriends = storage.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(user3, commonFriends.get(0));
    }

    @Test
    void shouldDeleteUser() {
        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> {
                    User userToAdd = User.builder()
                            .email("test@email.com")
                            .login("testLogin")
                            .name("testUsername")
                            .birthday(LocalDate.parse("2000-05-25"))
                            .build();
                    User user = storage.add(userToAdd);
                    int id = user.getId();
                    storage.delete(id);
                    storage.getUserById(id);
                }
        );
        assertEquals("Пользователь с id=1 не найден.", e.getMessage());
    }
}
