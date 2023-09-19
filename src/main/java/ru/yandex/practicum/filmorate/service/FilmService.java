package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("databaseFilm")
    private final FilmStorage storage;

    @Qualifier("databaseUser")
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;
    private final DirectorStorage directorStorage;
    private final GenreStorage genreStorage;
    private final FeedStorage feedStorage;

    public List<Film> getFilms() {
        return storage.getAll();
    }

    public Film add(Film film) {
        return storage.add(film);
    }

    public Film update(Film film) {
        return storage.update(film);
    }

    public void addLike(int filmId, int userId) {
        storage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likeStorage.addLike(filmId, userId);
        feedStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType("LIKE")
                .operation("ADD")
                .entityId(filmId)
                .timestamp(Date.from(Instant.now()))
                .build());
    }

    public void deleteLike(int filmId, int userId) {
        storage.getFilmById(filmId);
        userStorage.getUserById(userId);
        likeStorage.deleteLike(filmId, userId);
        feedStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType("LIKE")
                .operation("REMOVE")
                .entityId(filmId)
                .timestamp(Date.from(Instant.now()))
                .build());
    }

    public Film getFilmById(int id) {
        return storage.getFilmById(id);
    }

    public List<Film> getDirectorsFilms(int directorId, String sortBy) {
        directorStorage.getDirectorById(directorId);
        return storage.getDirectorsFilms(directorId, sortBy);
    }

    public List<Film> searchFilms(String query, String by) {
        return storage.search(query, by);
    }

    public void delete(int id) {
        storage.delete(id);
    }


    public List<Film> getCommonFilms(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        return storage.getCommonFilms(userId, friendId);
    }

    public List<Film> getRecommendations(int userId) {
        return storage.getRecommendations(userId);
    }

    public List<Film> getPopularsGenreAndYear(int limit, int genreId, int year) {
        if (genreId != -1) {
            genreStorage.getGenreById(genreId);
        }
        return storage.getPopularsGenreAndYear(limit, genreId, year);
    }
}
