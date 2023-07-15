package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private final UserValidator validator = new UserValidator();
    private int idCounter = 0;

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        setName(user);
        idCounter++;
        user.setId(idCounter);
        users.put(idCounter, user);
        log.info("Добавлен новый пользователь {}.", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        int id = user.getId();
        if (users.get(id) != null) {
            setName(user);
            users.put(id, user);
            log.info("Обновлен пользователь {}.", user);
        } else {
            String message = "Пользователь с id=" + id + " не найден.";
            log.warn(message);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        return user;
    }

    private void setName(User user) {
        if (user.getName() == null) user.setName(user.getLogin());
    }
}
