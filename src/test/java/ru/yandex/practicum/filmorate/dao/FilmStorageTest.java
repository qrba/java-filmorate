package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmStorageTest {
    private final FilmStorage storage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;
    private final LikeStorage likeStorage;

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
    void shouldGetMostPopular() {
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
    void shouldGetCommonFilms() {
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

    @Test
    public void searchForMovieByTitleTest() {
        directorStorage.addDirector(new Director(1, "DiReCtOrS bY sEaRcHiNg"));
        Film film1 = Film.builder()
                .id(1)
                .name("Film sEaRcHiNg")
                .description("Film by test")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(200)
                .mpa(new RatingMPA(1, "G"))
                .genres(new ArrayList<>())
                .directors(new ArrayList<>())
                .build();
        directorStorage.addFilmDirectors(film1, 1);
        storage.add(film1);

        directorStorage.addDirector(new Director(2, "no name"));
        Film film2 = Film.builder()
                .id(2)
                .name("no name")
                .description("Film by test")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(200)
                .mpa(new RatingMPA(1, "G"))
                .genres(new ArrayList<>())
                .directors(new ArrayList<>())
                .build();
        storage.add(film2);
        Film foundFilm1 = storage.search("search", "title").get(0);
        assertEquals(film1, foundFilm1);
    }

    @Test
    public void searchForMovieByDirectorTest() {
        directorStorage.addDirector(new Director(1, "DiReCtOrS bY sEaRcHiNg"));
        Film film1 = Film.builder()
                .id(1)
                .name("Film sEaRcHiNg")
                .description("Film by test")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(200)
                .mpa(new RatingMPA(1, "G"))
                .genres(new ArrayList<>())
                .directors(new ArrayList<>())
                .build();
        directorStorage.addFilmDirectors(film1, 1);
        storage.add(film1);

        directorStorage.addDirector(new Director(2, "no name"));
        List<Director> directors = new ArrayList<>();
        directors.add(new Director(2, "no name"));
        Film film2 = Film.builder()
                .id(2)
                .name("no name")
                .description("Film by test")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(200)
                .mpa(new RatingMPA(1, "G"))
                .genres(new ArrayList<>())
                .directors(new ArrayList<>())
                .build();
        storage.add(film2);
        film2.setDirectors(directors);
        directorStorage.addFilmDirectors(film2, film2.getId());
        Film foundFilm2 = storage.search("name", "director").get(0);
        assertEquals(film2, foundFilm2);
    }

    @Test
    public void searchForMovieByTitleAndDirectorTest() {
        directorStorage.addDirector(new Director(1, "DiReCtOrS1 bY sEaRcHiNg"));
        List<Director> directors = new ArrayList<>();
        directors.add(new Director(1, "DiReCtOrS1 bY sEaRcHiNg"));
        Film film1 = Film.builder()
                .id(1)
                .name("Film1")
                .description("Film1 by test")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(200)
                .mpa(new RatingMPA(1, "G"))
                .genres(new ArrayList<>())
                .directors(new ArrayList<>())
                .build();
        storage.add(film1);
        film1.setDirectors(directors);
        directorStorage.addFilmDirectors(film1, film1.getId());

        directorStorage.addDirector(new Director(2, "no name"));
        Film film2 = Film.builder()
                .id(2)
                .name("Film2 searching")
                .description("Film2 by test")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(200)
                .mpa(new RatingMPA(1, "G"))
                .genres(new ArrayList<>())
                .directors(new ArrayList<>())
                .build();
        storage.add(film2);
        List<Film> allFilm = new ArrayList<>();
        allFilm.add(film1);
        allFilm.add(film2);
        List<Film> foundFilm = storage.search("searc", "director,title");
        assertEquals(allFilm, foundFilm);
    }

    @Test
    void shouldGetRecommendations() {
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
        filmToAdd = Film.builder()
                .name("Film 2")
                .description("Film 2 is a test entity")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(190)
                .mpa(new RatingMPA(5, "NC-17"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film3 = storage.add(filmToAdd);
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
        likeStorage.addLike(film2.getId(), user1.getId());
        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film1.getId(), user2.getId());
        List<Film> recommendations = storage.getRecommendations(user2.getId());
        assertEquals(1, recommendations.size());
        assertEquals(film2, recommendations.get(0));
    }

    @Test
    void shouldGetRecommendationsEmptyList() {
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
        filmToAdd = Film.builder()
                .name("Film 2")
                .description("Film 2 is a test entity")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(190)
                .mpa(new RatingMPA(5, "NC-17"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film3 = storage.add(filmToAdd);
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
        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film1.getId(), user2.getId());
        List<Film> recommendations = storage.getRecommendations(user2.getId());

        assertEquals(0, recommendations.size());
    }

    @Test
    void shouldGetDirectorsFilms() {
        Director director = new Director(1, "Test name");
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Film 1 is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(List.of(director))
                .build();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Film 2 is a test entity")
                .releaseDate(LocalDate.parse("1996-10-20"))
                .duration(190)
                .mpa(new RatingMPA(5, "NC-17"))
                .genres(Collections.emptyList())
                .directors(List.of(director))
                .build();
        directorStorage.addDirector(director);
        storage.add(film1);
        storage.add(film2);

        assertEquals(2, storage.getDirectorsFilms(1, "yaer").size());
    }
}
