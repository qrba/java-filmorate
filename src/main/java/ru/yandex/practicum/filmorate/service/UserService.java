package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.service.validators.UserValidator.validate;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("databaseUser")
    private final UserStorage storage;

    public List<User> getUsers() {
        return storage.getAll();
    }

    public User add(User user) {
        validate(user);
        return storage.add(user);
    }

    public User update(User user) {
        validate(user);
        return storage.update(user);
    }

    public void addFriend(int id, int friendId) {
        storage.addFriend(id, friendId);
    }

    public void deleteFriend(int id, int friendId) {
        storage.deleteFriend(id, friendId);
    }

    public List<User> getFriends(int id) {
        return storage.getFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        return storage.getCommonFriends(id, otherId);
    }

    public User getUserById(int id) {
        return storage.getUserById(id);
    }
}
