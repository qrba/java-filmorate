package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                LocalDate.parse("1985-10-20"), 90);
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
                LocalDate.parse("1985-10-20"), 90);
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotAddFilmWhenIncorrectDescription() {
        Film film = new Film("Film", "Very very very very very very very very very very very very very " +
                "very very very very very very very very very very very very very very very very very very very very " +
                "very very very very very very very long description", LocalDate.parse("1985-10-20"), 90);
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotAddFilmWhenIncorrectReleaseDate() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1885-10-20"), 90);
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotAddFilmWhenIncorrectDuration() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), -90);
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90);
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film, Film.class);
        Film addedFilm = response.getBody();
        Film newFilm = new Film("Film Updated", "Film Updated is a test entity",
                LocalDate.parse("1995-10-20"), 190);

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
                LocalDate.parse("1995-10-20"), 190);
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
                LocalDate.parse("1985-10-20"), 90);
        ResponseEntity<Film> response = restTemplate.postForEntity(resource, film1, Film.class);
        film1 = response.getBody();
        Film film2 = new Film("Film 2", "Film 2 is a test entity",
                LocalDate.parse("1995-10-20"), 190);
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
}
