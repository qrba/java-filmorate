package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
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
        addEvent(userId, EventOperation.ADD, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        storage.deleteFriend(userId, friendId);
        addEvent(userId, EventOperation.REMOVE, friendId);
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

    private void addEvent(int userId, EventOperation operation, int eventId) {
        feedStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType(EventType.FRIEND)
                .operation(operation)
                .entityId(eventId)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }
}
