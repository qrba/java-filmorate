package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreStorageTest {
    private final GenreStorage storage;

    @Test
    void shouldGetAll() {
        List<Genre> genres = storage.getAll();
        List<Genre> testList = List.of(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма"),
                new Genre(3, "Мультфильм"),
                new Genre(4, "Триллер"),
                new Genre(5, "Документальный"),
                new Genre(6, "Боевик")
        );

        assertEquals(testList, genres);
    }

    @Test
    void shouldGetGenreById() {
        Genre genre = storage.getGenreById(1);

        assertEquals(new Genre(1, "Комедия"), genre);

        genre = storage.getGenreById(2);

        assertEquals(new Genre(2, "Драма"), genre);

        genre = storage.getGenreById(3);

        assertEquals(new Genre(3, "Мультфильм"), genre);

        genre = storage.getGenreById(4);

        assertEquals(new Genre(4, "Триллер"), genre);

        genre = storage.getGenreById(5);

        assertEquals(new Genre(5, "Документальный"), genre);

        genre = storage.getGenreById(6);

        assertEquals(new Genre(6, "Боевик"), genre);
    }

    @Test
    void shouldNotGetGenreWhenIncorrectId() {
        GenreNotFoundException e = Assertions.assertThrows(
                GenreNotFoundException.class,
                () -> storage.getGenreById(100)
        );

        assertEquals("Жанр с id=100 не найден.", e.getMessage());
    }
}
