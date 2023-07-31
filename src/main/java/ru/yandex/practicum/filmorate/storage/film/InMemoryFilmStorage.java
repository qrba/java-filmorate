package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.service.validators.FilmValidator.validate;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films;
    private int idCounter;

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film add(Film film) {
        validate(film);
        idCounter++;
        film.setId(idCounter);
        films.put(idCounter, film);
        log.info("Добавлен фильм {}.", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        validate(film);
        int id = film.getId();
        if (films.get(id) == null) throwFilmNotFoundException("Фильм с id=" + id + " не найден.");
        films.put(id, film);
        log.info("Обновлен фильм {}.", film);
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        Film film = films.get(id);
        if (film == null) throwFilmNotFoundException("Фильм с id=" + id + " не найден.");
        return film;
    }

    private void throwFilmNotFoundException(String message) {
        log.error(message);
        throw new FilmNotFoundException(message);
    }
}
