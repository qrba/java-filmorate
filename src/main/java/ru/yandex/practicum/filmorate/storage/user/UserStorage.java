package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAll();

    User add(User user);

    User update(User user);

    User getUserById(int id);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);

    List<User> getFriends(int id);

    List<User> getCommonFriends(int id, int otherId);
}
