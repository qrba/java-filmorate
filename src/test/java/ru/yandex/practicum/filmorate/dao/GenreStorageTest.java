package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreStorageTest {
    private final GenreStorage storage;
    private final FilmStorage filmStorage;

    @Test
    void shouldGetAll() {
        List<Genre> genres = storage.getAll();
        List<Genre> testList = List.of(
                Genre.builder().id(1).name("Комедия").build(),
                Genre.builder().id(2).name("Драма").build(),
                Genre.builder().id(3).name("Мультфильм").build(),
                Genre.builder().id(4).name("Триллер").build(),
                Genre.builder().id(5).name("Документальный").build(),
                Genre.builder().id(6).name("Боевик").build()
        );

        assertEquals(testList, genres);
    }

    @Test
    void shouldGetGenreById() {
        Genre genre = storage.getGenreById(1);

        assertEquals(Genre.builder().id(1).name("Комедия").build(), genre);

        genre = storage.getGenreById(2);

        assertEquals(Genre.builder().id(2).name("Драма").build(), genre);

        genre = storage.getGenreById(3);

        assertEquals(Genre.builder().id(3).name("Мультфильм").build(), genre);

        genre = storage.getGenreById(4);

        assertEquals(Genre.builder().id(4).name("Триллер").build(), genre);

        genre = storage.getGenreById(5);

        assertEquals(Genre.builder().id(5).name("Документальный").build(), genre);

        genre = storage.getGenreById(6);

        assertEquals(Genre.builder().id(6).name("Боевик").build(), genre);
    }

    @Test
    void shouldNotGetGenreWhenIncorrectId() {
        GenreNotFoundException e = Assertions.assertThrows(
                GenreNotFoundException.class,
                () -> storage.getGenreById(100)
        );

        assertEquals("Жанр с id=100 не найден.", e.getMessage());
    }

    @Test
    void shouldAddDeleteGetFilmGenres() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(List.of(Genre.builder().id(4).build(), Genre.builder().id(6).build()))
                .build();
        Film addedFilm = filmStorage.add(film);
        List<Genre> genres = storage.getFilmGenres(addedFilm.getId());

        assertEquals(
                List.of(
                        Genre.builder().id(4).name("Триллер").build(),
                        Genre.builder().id(6).name("Боевик").build()
                ),
                genres
        );

        storage.deleteFilmGenres(film.getId());
        genres = storage.getFilmGenres(film.getId());

        assertTrue(genres.isEmpty());
    }
}
