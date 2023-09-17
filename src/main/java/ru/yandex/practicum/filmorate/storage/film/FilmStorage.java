package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getAll();

    Film add(Film film);

    Film update(Film film);

    Film getFilmById(int id);

    List<Film> getMostPopular(int size);

    List<Film> search(String query, String by);

    void delete(int id);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getDirectorsFilms(int directorId, String sortBy);
}
