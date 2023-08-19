package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.service.validators.UserValidator.validate;

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
        validate(user);
        return storage.add(user);
    }

    public User update(User user) {
        validate(user);
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
}
