package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmStorageTest {
    @Qualifier("databaseFilm")
    private final FilmStorage storage;

    @Qualifier("databaseUser")
    private final UserStorage userStorage;

    @Test
    void shouldGetAll() {
        Film filmToAdd = Film.builder()
                .name("Film 1")
                .description("Film 1 is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film1 = storage.add(filmToAdd);
        filmToAdd = Film.builder()
                .name("Film 2")
                .description("Film 2 is a test entity")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(190)
                .mpa(new RatingMPA(5, "NC-17"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film2 = storage.add(filmToAdd);

        List<Film> films = storage.getAll();
        assertEquals(2, films.size());
        assertEquals(film1, films.get(0));
        assertEquals(film2, films.get(1));
    }

    @Test
    void shouldAdd() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film addedFilm = storage.add(film);
        film.setId(addedFilm.getId());

        assertEquals(film, addedFilm);
    }

    @Test
    void shouldUpdate() {
        Film filmToAdd = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film = storage.add(filmToAdd);
        Film newFilm = Film.builder()
                .id(film.getId())
                .name("Film Updated")
                .description("Film Updated is a test entity")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(190)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film updatedFilm = storage.update(newFilm);

        assertEquals(newFilm, updatedFilm);
    }

    @Test
    void shouldNotUpdateWhenIncorrectId() {
        FilmNotFoundException e = Assertions.assertThrows(
                FilmNotFoundException.class,
                () -> {
                    Film film = Film.builder()
                            .id(100)
                            .name("Film")
                            .description("Film is a test entity")
                            .releaseDate(LocalDate.parse("1985-10-20"))
                            .duration(90)
                            .mpa(new RatingMPA(1, "G"))
                            .genres(Collections.emptyList())
                            .directors(Collections.emptyList())
                            .build();
                    storage.update(film);
                }
        );

        assertEquals("Фильм с id 100 не существует", e.getMessage());
    }

    @Test
    void shouldGetFilmById() {
        Film filmToAdd = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film = storage.add(filmToAdd);

        Film filmById = storage.getFilmById(film.getId());
        assertEquals(film, filmById);
    }

    @Test
    void shouldNotGetFilmWhenIncorrectId() {
        FilmNotFoundException e = Assertions.assertThrows(
                FilmNotFoundException.class,
                () -> storage.getFilmById(100)
        );

        assertEquals("Фильм с id 100 не существует", e.getMessage());
    }

    @Test
    void shouldGetMostPopular(@Autowired LikeStorage likeStorage) {
        Film filmToAdd = Film.builder()
                .name("Film 1")
                .description("Film 1 is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film1 = storage.add(filmToAdd);
        filmToAdd = Film.builder()
                .name("Film 2")
                .description("Film 2 is a test entity")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(190)
                .mpa(new RatingMPA(5, "NC-17"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film2 = storage.add(filmToAdd);
        User userToAdd = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("testUsername")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User user = userStorage.add(userToAdd);
        likeStorage.addLike(film2.getId(), user.getId());
        List<Film> mostPopular = storage.getPopularsGenreAndYear(10, -1, -1);

        assertEquals(film2, mostPopular.get(0));
        assertEquals(film1, mostPopular.get(1));
    }

    @Test
    void shouldDeleteFilm() {
        FilmNotFoundException e = Assertions.assertThrows(
                FilmNotFoundException.class,
                () -> {
                    Film filmToAdd = Film.builder()
                            .name("Film")
                            .description("Film is a test entity")
                            .releaseDate(LocalDate.parse("1985-10-20"))
                            .duration(90)
                            .mpa(new RatingMPA(1, "G"))
                            .genres(Collections.emptyList())
                            .directors(Collections.emptyList())
                            .build();
                    Film film = storage.add(filmToAdd);
                    int id = film.getId();
                    storage.delete(id);
                    storage.getFilmById(id);
                }
        );

        assertEquals("Фильм с id 1 не существует", e.getMessage());
    }

    @Test
    void shouldGetCommonFilms(@Autowired LikeStorage likeStorage) {
        Film filmToAdd = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film = storage.add(filmToAdd);
        User userToAdd = User.builder()
                .email("test1@email.com")
                .login("testLogin1")
                .name("testUsername1")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User user1 = userStorage.add(userToAdd);
        userToAdd = User.builder()
                .email("test2@email.com")
                .login("testLogin2")
                .name("testUsername2")
                .birthday(LocalDate.parse("1987-01-10"))
                .build();
        User user2 = userStorage.add(userToAdd);
        likeStorage.addLike(film.getId(), user1.getId());
        List<Film> commonFilms = storage.getCommonFilms(user1.getId(), user2.getId());

        assertEquals(0, commonFilms.size());

        likeStorage.addLike(film.getId(), user2.getId());
        commonFilms = storage.getCommonFilms(user1.getId(), user2.getId());

        assertEquals(1, commonFilms.size());
        assertEquals(film, commonFilms.get(0));
    }
}
