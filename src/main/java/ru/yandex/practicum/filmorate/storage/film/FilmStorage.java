package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getAll();

    Film add(Film film);

    Film update(Film film);

    Film getFilmById(int id);

    void addLike(int id, int userId);

    void deleteLike(int id, int userId);

    List<Film> getMostPopular(int size);
}
