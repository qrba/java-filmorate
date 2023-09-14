package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.service.validators.FilmValidator.validate;

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
        validate(film);
        return genreStorage.addFilmGenres(storage.add(film));
    }

    public Film update(Film film) {
        validate(film);
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

    public List<Film> getFilmsRecommendations(int userId) {
        List<User> users = userStorage.getAll();
        User user = userStorage.getUserById(userId);
        List<Integer> favoriteFilms = likeStorage.getListsOfFavoriteFilms(userId);
        int maxNumberOfCoincidences = 0;
        int coincidences = 0;
        User similarUser = user;
        for (User userOther : users) {
            if (userOther.getId() == user.getId()) {
                continue;
            }
            List<Integer> favoriteMovies = likeStorage.getListsOfFavoriteFilms(userOther.getId());
            for (Integer favoriteFilm : favoriteFilms) {
                if (favoriteMovies.contains(favoriteFilm)) {
                    coincidences++;
                }
            }
            if (coincidences > maxNumberOfCoincidences &&
                    favoriteMovies.stream().anyMatch(id -> !favoriteFilms.contains(id))) {
                maxNumberOfCoincidences = coincidences;
                similarUser = userOther;
            }
            coincidences = 0;
        }
        List<Film> recommendations = likeStorage.getListsOfFavoriteFilms(similarUser.getId()).stream()
                .map(storage::getFilmById)
                .filter(film -> !favoriteFilms.contains(film.getId()))
                .collect(Collectors.toList());
        recommendations.forEach(film -> film.setGenres(genreStorage.getFilmGenres(film.getId())));
        return recommendations;
    }
}
