package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users;
    private final UserValidator validator;
    private int idCounter;

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User add(User user) {
        validator.validate(user);
        setName(user);
        idCounter++;
        user.setId(idCounter);
        users.put(idCounter, user);
        log.info("Добавлен новый пользователь {}.", user);
        return user;
    }

    @Override
    public User update(User user) {
        validator.validate(user);
        int id = user.getId();
        if (users.get(id) == null) throwUserNotFoundException("Пользователь с id=" + id + " не найден.");
        setName(user);
        users.put(id, user);
        log.info("Обновлен пользователь {}.", user);
        return user;
    }

    @Override
    public User getUserById(int id) {
        User user = users.get(id);
        if (user == null) throwUserNotFoundException("Пользователь с id=" + id + " не найден.");
        return user;
    }

    private void setName(User user) {
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
    }

    private void throwUserNotFoundException(String message) {
        log.error(message);
        throw new UserNotFoundException(message);
    }
}
