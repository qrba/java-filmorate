package ru.yandex.practicum.filmorate.service.validators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ModelValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
public class FilmValidator {
    public static void validate(Film film) {
        if (film.getName().isEmpty()) throw new ModelValidationException("Пустое имя фильма.");

        if (film.getDescription().length() > 200)
            throw new ModelValidationException("Превышен допустимый размер описания.");

        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28")))
            throw new ModelValidationException(
                    "Дата релиза не может быть раньше даты релиза первого фильма в истории."
            );

        if (film.getDuration() <= 0) throw new ModelValidationException("Отрицательная продолжительность фильма.");
    }
}
