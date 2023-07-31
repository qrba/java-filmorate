package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.service.validators.UserValidator.validate;

@Service
@RequiredArgsConstructor
public class UserService {
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
        User user = storage.getUserById(id);
        User friend = storage.getUserById(friendId);
        user.addFriend(friendId);
        friend.addFriend(id);
    }

    public void deleteFriend(int id, int friendId) {
        User user = storage.getUserById(id);
        User friend = storage.getUserById(friendId);
        user.deleteFriend(friendId);
        friend.deleteFriend(id);
    }

    public List<User> getFriends(int id) {
        return storage.getUserById(id).getFriends().stream()
                .map(storage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int id, int otherId) {
        return storage.getUserById(id).getFriends().stream()
                .filter(storage.getUserById(otherId).getFriends()::contains)
                .map(storage::getUserById)
                .collect(Collectors.toList());
    }

    public User getUserById(int id) {
        return storage.getUserById(id);
    }
}
