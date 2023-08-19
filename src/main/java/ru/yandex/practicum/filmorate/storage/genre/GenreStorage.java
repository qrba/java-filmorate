package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    List<Genre> getAll();

    Genre getGenreById(int id);

    Film addFilmGenres(Film film);

    List<Genre> getFilmGenres(int filmId);

    void deleteFilmGenres(int filmId);
}
