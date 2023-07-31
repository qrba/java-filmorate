package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage storage;
    private final UserStorage userStorage;

    public List<Film> getFilms() {
        return storage.getAll();
    }

    public Film add(Film film) {
        return storage.add(film);
    }

    public Film update(Film film) {
        return storage.update(film);
    }

    public void addLike(int id, int userId) {
        Film film = storage.getFilmById(id);
        User user = userStorage.getUserById(userId);
        film.addLike(userId);
    }

    public void deleteLike(int id, int userId) {
        Film film = storage.getFilmById(id);
        User user = userStorage.getUserById(userId);
        film.deleteLike(userId);
    }

    public List<Film> getMostPopular(int size) {
        return storage.getAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikesNumber).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }

    public Film getFilmById(int id) {
        return storage.getFilmById(id);
    }
}
