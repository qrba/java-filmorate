package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("databaseUser")
    private final UserStorage storage;

    private final FriendStorage friendStorage;

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
        friendStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        friendStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriends(int id) {
        getUserById(id);
        return friendStorage.getFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        getUserById(id);
        getUserById(otherId);
        return friendStorage.getCommonFriends(id, otherId);
    }

    public List<Feed> getUserFeed(Integer userId) {
        try {
            getUserById(userId);
            return storage.getUserFeed(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден.");
        }
    }
}
