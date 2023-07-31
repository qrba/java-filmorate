package ru.yandex.practicum.filmorate.service.validators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class UserValidator {
    public static void validate(User user) {
        if (!user.getEmail().contains("@")) throwValidationException("Некорректный email пользователя.");
        String login = user.getLogin();
        if (login.isEmpty() || login.contains(" ")) throwValidationException("Некорректный логин пользователя.");
        if (user.getName() == null) user.setName(user.getLogin());
        if (user.getBirthday().isAfter(LocalDate.now()))
            throwValidationException("Некорректная дата рождения пользователя.");
    }

    private static void throwValidationException(String message) {
        log.warn(message);
        throw new ValidationException(message);
    }
}