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
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmControllerTest {
    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void addFilm() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        Film addedFilm = response.getBody();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(addedFilm);
        assertEquals(addedFilm.getId(), 1);
        assertEquals(addedFilm.getName(), film.getName());
        assertEquals(addedFilm.getDescription(), film.getDescription());
        assertEquals(addedFilm.getReleaseDate(), film.getReleaseDate());
        assertEquals(addedFilm.getDuration(), film.getDuration());

        film = new Film("", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90);
        response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        film = new Film("Film", "Very very very very very very very very very very very very very " +
                "very very very very very very very very very very very very very very very very very very very very " +
                "very very very very very very very long description", LocalDate.parse("1985-10-20"), 90);
        response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1885-10-20"), 90);
        response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), -90);
        response = restTemplate.postForEntity("/films", film, Film.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateFilm() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        Film addedFilm = response.getBody();
        Film newFilm = new Film("Film Updated", "Film Updated is a test entity",
                LocalDate.parse("1995-10-20"), 190);

        assertNotNull(addedFilm);

        newFilm.setId(addedFilm.getId());
        response = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(newFilm),
                Film.class
        );
        Film updatedFilm = response.getBody();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(updatedFilm);
        assertEquals(updatedFilm.getId(), addedFilm.getId());
        assertEquals(updatedFilm.getName(), newFilm.getName());
        assertEquals(updatedFilm.getDescription(), newFilm.getDescription());
        assertEquals(updatedFilm.getReleaseDate(), newFilm.getReleaseDate());
        assertEquals(updatedFilm.getDuration(), newFilm.getDuration());

        newFilm.setId(999);
        response = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(newFilm),
                Film.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void getFilms() {
        ResponseEntity<Film[]> response = restTemplate.getForEntity("/films", Film[].class);
        Film[] films = response.getBody();
        Film film1 = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90);
        film1.setId(1);
        Film film2 = new Film("Film Updated", "Film Updated is a test entity",
                LocalDate.parse("1995-10-20"), 190);
        film2.setId(2);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(films);
        assertEquals(films.length, 2);
        assertEquals(films[0], film1);
        assertEquals(films[1], film2);
    }
}
