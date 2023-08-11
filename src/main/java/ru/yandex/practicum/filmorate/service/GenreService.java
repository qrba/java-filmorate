package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    @Qualifier("databaseGenre")
    private final GenreStorage storage;

    public List<Genre> getGenres() {
        return storage.getAll();
    }

    public Genre getGenreById(int id) {
        return storage.getGenreById(id);
    }
}
