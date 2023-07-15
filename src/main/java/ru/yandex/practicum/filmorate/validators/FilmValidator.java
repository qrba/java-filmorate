package ru.yandex.practicum.filmorate.validators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
public class FilmValidator {
    public void validate(Film film) {
        if (film.getName().isEmpty()) throwValidationException("Пустое имя фильма.");
        if (film.getDescription().length() > 200) throwValidationException("Превышен допустимый размер описания.");
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28")))
            throwValidationException("Дата релиза не может быть раньше даты релиза первого фильма в истории.");
        if (film.getDuration() <= 0) throwValidationException("Отрицательная продолжительность фильма.");
    }

    private void throwValidationException(String message) {
        log.warn(message);
        throw new ValidationException(message);
    }
}
