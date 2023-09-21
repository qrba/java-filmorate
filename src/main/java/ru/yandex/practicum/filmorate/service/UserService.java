package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage storage;
    private final FeedStorage feedStorage;

    public List<User> getUsers() {
        return storage.getAll();
    }

    public User getUserById(int id) {
        return storage.getUserById(id);
    }

    public User add(User user) {
        return storage.add(user);
    }

    public User update(User user) {
        return storage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        storage.addFriend(userId, friendId);
        feedStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType("FRIEND")
                .operation("ADD")
                .entityId(friendId)
                .timestamp(Date.from(Instant.now()))
                .build());

    }

    public void deleteFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        storage.deleteFriend(userId, friendId);
        feedStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType("FRIEND")
                .operation("REMOVE")
                .entityId(friendId)
                .timestamp(Date.from(Instant.now()))
                .build());
    }

    public List<User> getFriends(int id) {
        getUserById(id);
        return storage.getFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        getUserById(id);
        getUserById(otherId);
        return storage.getCommonFriends(id, otherId);
    }

    public void delete(int id) {
        storage.delete(id);
    }

    public List<Event> getUserFeed(int userId) {
        getUserById(userId);
        return feedStorage.getUserFeed(userId);
    }
}
