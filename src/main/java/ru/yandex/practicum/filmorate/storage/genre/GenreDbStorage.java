package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.util.FilmorateMapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        String sqlQuery = "select * from genres";
        return jdbcTemplate.query(
                sqlQuery,
                FilmorateMapper::genreFromRow
        );
    }

    @Override
    public Genre getGenreById(int id) {
        try {
            String sqlQuery = "select * from genres where id = ?";
            return jdbcTemplate.queryForObject(
                    sqlQuery,
                    FilmorateMapper::genreFromRow,
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new GenreNotFoundException("Жанр с id=" + id + " не найден.");
        }
    }

    @Override
    public List<Genre> getFilmGenres(int filmId) {
        return jdbcTemplate.query(
                "select g.* from genres as g join film_genres as fg on g.id = fg.genre_id where fg.film_id = ?",
                FilmorateMapper::genreFromRow,
                filmId
        );
    }

    @Override
    public Film addFilmGenres(Film film) {
        List<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            genres = genres.stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .collect(Collectors.toList());
            film.setGenres(genres);
            String sqlQuery = "insert into film_genres (film_id, genre_id) values (?, ?)";
            int filmId = film.getId();
            genres.forEach(genre -> jdbcTemplate.update(sqlQuery, filmId, genre.getId()));
        }
        return film;
    }

    @Override
    public void deleteFilmGenres(int filmId) {
        jdbcTemplate.update("delete from film_genres where film_id = ?", filmId);
    }
}
