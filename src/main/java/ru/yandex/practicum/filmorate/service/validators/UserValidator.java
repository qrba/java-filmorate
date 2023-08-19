package ru.yandex.practicum.filmorate.service.validators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ModelValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class UserValidator {
    public static void validate(User user) {
        if (!user.getEmail().contains("@")) throw new ModelValidationException("Некорректный email пользователя.");

        String login = user.getLogin();
        if (login.isEmpty() || login.contains(" "))
            throw new ModelValidationException("Некорректный логин пользователя.");

        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());

        if (user.getBirthday().isAfter(LocalDate.now()))
            throw new ModelValidationException("Некорректная дата рождения пользователя.");
    }
}