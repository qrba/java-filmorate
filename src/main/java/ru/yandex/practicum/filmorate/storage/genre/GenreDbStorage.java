package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("databaseGenre")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        String sqlQuery = "select * from genres";
        return jdbcTemplate.query(sqlQuery, this::mapFromRow);
    }

    @Override
    public Genre getGenreById(int id) {
        try {
            String sqlQuery = "select * from genres where id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new GenreNotFoundException("Жанр с id=" + id + " не найден.");
        }
    }

    private Genre mapFromRow(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }
}
