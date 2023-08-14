package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FriendStorageTest {
    private final FriendStorage storage;

    @Test
    void shouldAddDeleteGetFriend(@Qualifier("databaseUser") UserStorage userStorage) {
        User user1 = new User(1, "test1@email.com", "testLogin1",
                "testName1", LocalDate.parse("2000-05-25"));
        User user2 = new User(2, "test2@email.com", "testLogin2",
                "testName2", LocalDate.parse("1990-06-11"));
        userStorage.add(user1);
        userStorage.add(user2);
        storage.addFriend(user1.getId(), user2.getId());
        List<User> friends = storage.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertEquals(user2, friends.get(0));
    }

    @Test
    void shouldGetCommonFriends(@Qualifier("databaseUser") UserStorage userStorage) {
        User user1 = new User(1, "test1@email.com", "testLogin1",
                "testName1", LocalDate.parse("2000-05-25"));
        User user2 = new User(2, "test2@email.com", "testLogin2",
                "testName2", LocalDate.parse("1990-06-11"));
        User user3 = new User(3, "test3@email.com", "testLogin3",
                "testName3", LocalDate.parse("1995-08-02"));
        userStorage.add(user1);
        userStorage.add(user2);
        userStorage.add(user3);
        storage.addFriend(user1.getId(), user2.getId());
        storage.addFriend(user1.getId(), user3.getId());
        storage.addFriend(user2.getId(), user3.getId());
        List<User> commonFriends = storage.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(user3, commonFriends.get(0));
    }
}
