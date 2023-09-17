package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("databaseUser")
    private final UserStorage storage;

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
    }

    public void deleteFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        storage.deleteFriend(userId, friendId);
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
}
