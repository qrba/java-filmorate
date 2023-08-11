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
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        Film film1 = new Film("Film 1", "Film 1 is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        storage.add(film1);
        Film film2 = new Film("Film 2", "Film 2 is a test entity",
                LocalDate.parse("1995-10-20"), 190, new RatingMPA(5, "NC-17"));
        storage.add(film2);

        List<Film> films = storage.getAll();
        assertEquals(2, films.size());
        assertEquals(film1, films.get(0));
        assertEquals(film2, films.get(1));
    }

    @Test
    void shouldAdd() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        Film addedFilm = storage.add(film);

        assertEquals(film, addedFilm);
    }

    @Test
    void shouldUpdate() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        storage.add(film);
        Film newFilm = new Film("Film Updated", "Film Updated is a test entity",
                LocalDate.parse("1995-10-20"), 190, new RatingMPA(1, "G"));
        newFilm.setId(film.getId());
        Film updatedFilm = storage.update(newFilm);

        assertEquals(newFilm, updatedFilm);
    }

    @Test
    void shouldNotUpdateWhenIncorrectId() {
        FilmNotFoundException e = Assertions.assertThrows(
                FilmNotFoundException.class,
                () -> {
                    Film film = new Film("Film", "Film is a test entity",
                            LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
                    film.setId(100);
                    storage.update(film);
                }
        );

        assertEquals("Фильм с id=100 не найден.", e.getMessage());
    }

    @Test
    void shouldGetFilmById() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        storage.add(film);

        Film filmById = storage.getFilmById(film.getId());
        assertEquals(film, filmById);
    }

    @Test
    void shouldNotGetFilmWhenIncorrectId() {
        FilmNotFoundException e = Assertions.assertThrows(
                FilmNotFoundException.class,
                () -> storage.getFilmById(100)
        );

        assertEquals("Фильм с id=100 не найден.", e.getMessage());
    }

    @Test
    void shouldAddLike() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        storage.add(film);
        User user = new User(1, "test@email.com", "testLogin",
                "testUsername", LocalDate.parse("2000-05-25"));
        userStorage.add(user);

        assertDoesNotThrow(() -> storage.addLike(film.getId(), user.getId()));
    }

    @Test
    void shouldNotAddLikeWhenIncorrectId() {
        FilmNotFoundException e = Assertions.assertThrows(
                FilmNotFoundException.class,
                () -> storage.addLike(1, 1)
        );

        assertEquals("Фильм с id=1 не найден.", e.getMessage());
    }

    @Test
    void shouldDeleteLike() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        storage.add(film);
        User user = new User(1, "test@email.com", "testLogin",
                "testUsername", LocalDate.parse("2000-05-25"));
        userStorage.add(user);
        storage.addLike(film.getId(), user.getId());

        assertDoesNotThrow(() -> storage.deleteLike(film.getId(), user.getId()));
    }

    @Test
    void shouldGetMostPopular() {
        Film film1 = new Film("Film 1", "Film 1 is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        storage.add(film1);
        Film film2 = new Film("Film 2", "Film 2 is a test entity",
                LocalDate.parse("1995-10-20"), 190, new RatingMPA(5, "NC-17"));
        storage.add(film2);
        User user = new User(1, "test@email.com", "testLogin",
                "testUsername", LocalDate.parse("2000-05-25"));
        userStorage.add(user);
        storage.addLike(film2.getId(), user.getId());
        List<Film> mostPopular = storage.getMostPopular(10);

        assertEquals(film2, mostPopular.get(0));
        assertEquals(film1, mostPopular.get(1));
    }
}