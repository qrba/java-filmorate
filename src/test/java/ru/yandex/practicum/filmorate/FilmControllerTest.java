package ru.yandex.practicum.filmorate;

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
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();

        assertNotNull(addedFilm);

        film.setId(addedFilm.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(film, addedFilm);
    }

    @Test
    void shouldNotAddFilmWhenIncorrectName() {
        Film film = new Film("", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldNotAddFilmWhenIncorrectDescription() {
        Film film = new Film("Film", "Very very very very very very very very very very very very " +
                "very very very very very very very very very very very very very very very very very very very very " +
                "very very very very very very very very long description",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldNotAddFilmWhenIncorrectReleaseDate() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1885-10-20"), 90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldNotAddFilmWhenIncorrectDuration() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), -90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();
        Film newFilm = new Film("Film Updated", "Film Updated is a test entity",
                LocalDate.parse("1995-10-20"), 190, new RatingMPA(1, "G"));

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
        Film newFilm = new Film("Film Updated", "Film Updated is a test entity",
                LocalDate.parse("1995-10-20"), 190, new RatingMPA(1, "G"));
        newFilm.setId(999);
        ResponseEntity<Film> response = restTemplate.exchange(
                resource,
                HttpMethod.PUT,
                new HttpEntity<>(newFilm),
                Film.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetFilms() {
        Film film1 = new Film("Film 1", "Film 1 is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film1, Film.class);
        film1 = response.getBody();
        Film film2 = new Film("Film 2", "Film 2 is a test entity",
                LocalDate.parse("1995-10-20"), 190, new RatingMPA(1, "G"));
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
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
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
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();

        assertNotNull(addedFilm);

        User user = new User(1, "test@email.com", "testLoginAndName",
                null, LocalDate.parse("2000-05-25"));
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
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();

        assertNotNull(addedFilm);

        User user = new User(1, "test@email.com", "testLoginAndName",
                null, LocalDate.parse("2000-05-25"));
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
        assertEquals(0,receivedFilm.getLikesNumber());
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
        Film film1 = new Film("Film 1", "Film 1 is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        Film film2 = new Film("Film 2", "Film 2 is a test entity",
                LocalDate.parse("1993-05-17"), 110, new RatingMPA(1, "G"));
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film1, Film.class);
        film1 = response.getBody();
        response = restTemplate.postForEntity(resource, film2, Film.class);
        film2 = response.getBody();

        assertNotNull(film1);
        assertNotNull(film2);

        User user1 = new User(1, "test1@email.com", "test1LoginAndName",
                null, LocalDate.parse("2000-05-25"));
        User user2 = new User(2, "test2@email.com", "test2LoginAndName",
                null, LocalDate.parse("1983-11-06"));
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
