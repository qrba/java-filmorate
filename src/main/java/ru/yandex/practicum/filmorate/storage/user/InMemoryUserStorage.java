package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component("memoryUser")
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users;
    private int idCounter;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User add(User user) {
        idCounter++;
        user.setId(idCounter);
        users.put(idCounter, user);
        log.info("Добавлен новый пользователь {}.", user);
        return user;
    }

    @Override
    public User update(User user) {
        int id = user.getId();
        if (users.get(id) == null) throw new UserNotFoundException("Пользователь с id=" + id + " не найден.");
        users.put(id, user);
        log.info("Обновлен пользователь {}.", user);
        return user;
    }

    @Override
    public User getUserById(int id) {
        User user = users.get(id);
        if (user == null) throw new UserNotFoundException("Пользователь с id=" + id + " не найден.");
        return user;
    }

    @Override
    public void addFriend(int id, int friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        user.addFriend(friendId);
        friend.addFriend(id);
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        user.deleteFriend(friendId);
        friend.deleteFriend(id);
    }

    @Override
    public List<User> getFriends(int id) {
        return getUserById(id).getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        return getUserById(id).getFriends().stream()
                .filter(this.getUserById(otherId).getFriends()::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}
