package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {
    List<Director> getAll();

    Director getDirectorById(int id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(int id);

    List<Director> getFilmDirectors(int filmId);

    void addFilmDirectors(Film film);
}
