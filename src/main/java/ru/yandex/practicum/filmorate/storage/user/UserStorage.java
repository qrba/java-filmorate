package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAll();

    User add(User user);

    User update(User user);

    User getUserById(int id);

    void delete(int id);
}
