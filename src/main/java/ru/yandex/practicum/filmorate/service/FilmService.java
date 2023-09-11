package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("databaseFilm")
    private final FilmStorage storage;

    @Qualifier("databaseUser")
    private final UserStorage userStorage;

    private final LikeStorage likeStorage;

    private final GenreStorage genreStorage;

    public List<Film> getFilms() {
        List<Film> films = storage.getAll();
        films.forEach(film -> film.setGenres(genreStorage.getFilmGenres(film.getId())));
        return films;
    }

    public Film add(Film film) {
        return genreStorage.addFilmGenres(storage.add(film));
    }

    public Film update(Film film) {
        genreStorage.deleteFilmGenres(film.getId());
        return genreStorage.addFilmGenres(storage.update(film));
    }

    public void addLike(int filmId, int userId) {
        storage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likeStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        storage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likeStorage.deleteLike(filmId, userId);
    }

    public List<Film> getMostPopular(int size) {
        List<Film> films = storage.getMostPopular(size);
        films.forEach(film -> film.setGenres(genreStorage.getFilmGenres(film.getId())));
        return films;
    }

    public Film getFilmById(int id) {
        Film film = storage.getFilmById(id);
        film.setGenres(genreStorage.getFilmGenres(film.getId()));
        return film;
    }
}
