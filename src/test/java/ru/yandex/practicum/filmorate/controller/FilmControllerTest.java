package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmControllerTest {
    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    private final String resource = "/films";

    @Test
    void shouldAddFilm() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();

        assertNotNull(addedFilm);

        film.setId(addedFilm.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(film, addedFilm);
    }

    @Test
    void shouldNotAddFilmWhenIncorrectName() {
        Film film = Film.builder()
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldNotAddFilmWhenIncorrectDescription() {
        Film film = Film.builder()
                .name("Film")
                .description("Very very very very very very very very very very very very very very very very very " +
                        "very very very very very very very very very very very very very very very very very very " +
                        "very very very very very long description")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldNotAddFilmWhenIncorrectReleaseDate() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1885-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldNotAddFilmWhenIncorrectDuration() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(-90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();
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

        assertNotNull(addedFilm);

        newFilm.setId(addedFilm.getId());
        response = restTemplate.exchange(
                resource,
                HttpMethod.PUT,
                new HttpEntity<>(newFilm),
                Film.class
        );
        Film updatedFilm = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(updatedFilm);
        assertEquals(newFilm, updatedFilm);
    }

    @Test
    void shouldNotUpdateFilmWhenIncorrectId() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.exchange(
                resource,
                HttpMethod.PUT,
                new HttpEntity<>(film),
                Film.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetFilms() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Film 1 is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film1, Film.class);
        film1 = response.getBody();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Film 2 is a test entity")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(190)
                .mpa(new RatingMPA(5, "NC-17"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        response = restTemplate.postForEntity(resource, film2, Film.class);
        film2 = response.getBody();
        ResponseEntity<Film[]> getResponse = restTemplate.getForEntity(resource, Film[].class);
        Film[] films = getResponse.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(films);
        assertEquals(2, films.length);
        assertEquals(film1, films[0]);
        assertEquals(film2, films[1]);
    }

    @Test
    void shouldGetFilmById() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();

        assertNotNull(addedFilm);

        ResponseEntity<Film> getResponse = restTemplate.getForEntity(resource + "/" + addedFilm.getId(), Film.class);
        Film receivedFilm = getResponse.getBody();

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(addedFilm, receivedFilm);
    }

    @Test
    void shouldNotGetFilmByIdWhenIncorrectId() {
        ResponseEntity<Film> response = restTemplate.getForEntity(resource + "/" + 1, Film.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldAddLike() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();
        System.out.println(addedFilm);

        assertNotNull(addedFilm);

        User user = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("testUsername")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        ResponseEntity<User> userResponse = restTemplate.postForEntity("/users", user, User.class);
        User addedUser = userResponse.getBody();
        System.out.println(addedUser);

        assertNotNull(addedUser);

        ResponseEntity<Void> voidResponse = restTemplate.exchange(
                resource + "/" + addedFilm.getId() + "/like/" + addedUser.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());
    }

    @Test
    void shouldNotAddLikeWhenIncorrectId() {
        ResponseEntity<Void> response = restTemplate.exchange(
                resource + "/1/like/1",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldDeleteLike() {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();

        assertNotNull(addedFilm);

        User user = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("testUsername")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        ResponseEntity<User> responseUser = restTemplate.postForEntity("/users", user, User.class);
        User addedUser = responseUser.getBody();

        assertNotNull(addedUser);

        ResponseEntity<Void> voidResponse = restTemplate.exchange(
                resource + "/" + addedFilm.getId() + "/like/" + addedUser.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());

        voidResponse = restTemplate.exchange(
                resource + "/" + addedFilm.getId() + "/like/" + addedUser.getId(),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class
        );
        ResponseEntity<Film> getResponse = restTemplate.getForEntity(resource + "/" + addedFilm.getId(), Film.class);
        Film receivedFilm = getResponse.getBody();

        assertNotNull(receivedFilm);
        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());
    }

    @Test
    void shouldNotDeleteLikeWhenIncorrectId() {
        ResponseEntity<Void> response = restTemplate.exchange(
                resource + "/1/like/1",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetMostPopular() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Film 1 is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Film 2 is a test entity")
                .releaseDate(LocalDate.parse("1995-10-20"))
                .duration(190)
                .mpa(new RatingMPA(5, "NC-17"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film1, Film.class);
        film1 = response.getBody();
        response = restTemplate.postForEntity(resource, film2, Film.class);
        film2 = response.getBody();

        assertNotNull(film1);
        assertNotNull(film2);

        User user1 = User.builder()
                .email("test1@email.com")
                .login("testLogin1")
                .name("testUsername1")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User user2 = User.builder()
                .email("test2@email.com")
                .login("testLogin2")
                .name("testUsername2")
                .birthday(LocalDate.parse("1987-01-10"))
                .build();
        ResponseEntity<User> responseUser = restTemplate.postForEntity("/users", user1, User.class);
        user1 = responseUser.getBody();
        responseUser = restTemplate.postForEntity("/users", user2, User.class);
        user2 = responseUser.getBody();

        assertNotNull(user1);
        assertNotNull(user2);

        ResponseEntity<Void> voidResponse = restTemplate.exchange(
                resource + "/" + film1.getId() + "/like/" + user1.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());

        voidResponse = restTemplate.exchange(
                resource + "/" + film1.getId() + "/like/" + user2.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());

        voidResponse = restTemplate.exchange(
                resource + "/" + film2.getId() + "/like/" + user1.getId(),
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                Void.class
        );

        assertEquals(HttpStatus.OK, voidResponse.getStatusCode());

        ResponseEntity<Film[]> getResponse = restTemplate.getForEntity(resource + "/popular", Film[].class);
        Film[] popular = getResponse.getBody();

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(popular);
        assertEquals(2, popular.length);
        assertEquals(film1, popular[0]);
        assertEquals(film2, popular[1]);
    }
}
