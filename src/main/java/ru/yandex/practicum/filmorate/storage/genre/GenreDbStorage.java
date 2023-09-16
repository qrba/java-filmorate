package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.util.FilmorateMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
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
                this::genreFromRow
        );
    }

    @Override
    public Genre getGenreById(int id) {
        try {
            String sqlQuery = "select * from genres where id = ?";
            return jdbcTemplate.queryForObject(
                    sqlQuery,
                    this::genreFromRow,
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new GenreNotFoundException("Жанр с id=" + id + " не найден.");
        }
    }

    @Override
    public List<Genre> getFilmGenres(int filmId) {
        String sqlQuery = "select * from genres where id in (select genre_id from film_genres where film_id = ?)";
        List<Genre> g = jdbcTemplate.query(sqlQuery, this::genreFromRow, filmId);
        return g;
    }

    @Override
    public void addFilmGenres(Film film, int filmId) {
        if (!getFilmGenres(film.getId()).isEmpty()) {
            String sqlQuery = "delete from film_genres where film_id = ?";
            jdbcTemplate.update(sqlQuery, filmId);
        }
        if (film.getGenres() == null) {
            return;
        }
        String sqlQuery = "insert into film_genres(film_id, genre_id) values (?, ?)";
        film.getGenres().forEach(genre -> jdbcTemplate.update(sqlQuery, filmId, genre.getId()));
    }

    @Override
    public void deleteFilmGenres(int filmId) {
        jdbcTemplate.update("delete from film_genres where film_id = ?", filmId);
    }

    private Genre genreFromRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
