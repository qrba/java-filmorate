package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.service.validators.FilmValidator.validate;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("databaseFilm")
    private final FilmStorage storage;

    @Qualifier("databaseUser")
    private final UserStorage userStorage;

    public List<Film> getFilms() {
        return storage.getAll();
    }

    public Film add(Film film) {
        validate(film);
        return storage.add(film);
    }

    public Film update(Film film) {
        validate(film);
        return storage.update(film);
    }

    public void addLike(int id, int userId) {
        userStorage.getUserById(userId);
        storage.addLike(id, userId);
    }

    public void deleteLike(int id, int userId) {
        userStorage.getUserById(userId);
        storage.deleteLike(id, userId);
    }

    public List<Film> getMostPopular(int size) {
        return storage.getMostPopular(size);
    }

    public Film getFilmById(int id) {
        return storage.getFilmById(id);
    }
}
