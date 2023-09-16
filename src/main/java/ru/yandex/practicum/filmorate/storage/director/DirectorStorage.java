package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Directors;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {
    List<Directors> getAll();

    Directors getDirectorById(int id);

    Directors addDirector(Directors directors);

    Directors updateDirector(Directors directors);

    void deleteDirector(int id);

    List<Directors> getFilmDirectors(int filmId);

    void addFilmDirectors(Film film, int filmId);
}
