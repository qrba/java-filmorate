package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Directors;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("dbDirector")
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Directors> getAll() {
        String sqlQuery = "select * from directors";
        return jdbcTemplate.query(
                sqlQuery,
                this::directorFromRow
        );
    }

    @Override
    public Directors getDirectorById(int id) {
        try {
            String sqlQuery = "select * from directors where id = ?";
            return jdbcTemplate.queryForObject(
                    sqlQuery,
                    this::directorFromRow,
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DirectorNotFoundException("Режиссер с id=" + id + " не найден.");
        }
    }

    @Override
    public Directors addDirector(Directors directors) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");
        int id = simpleJdbcInsert.executeAndReturnKey(directorToRow(directors)).intValue();
        log.info("Добавлен новый режиссер {}.", getDirectorById(id));
        return getDirectorById(id);
    }

    @Override
    public Directors updateDirector(Directors directors) {
        String sqlQuery = "update directors set id = ?, name = ?";
        try {
            getDirectorById(directors.getId());
        } catch (DirectorNotFoundException e) {
            log.debug("Обновление режиссера c неверным id: {}", directors.getId());
            throw new DirectorNotFoundException("Режиссер с id " + directors.getId() + " не существует");
        }
        jdbcTemplate.update(sqlQuery,
                directors.getId(),
                directors.getName());
        return getDirectorById(directors.getId());
    }

    @Override
    public void deleteDirector(int id) {
        String sqlQuery = "delete from directors where id = ?";
        try {
            getDirectorById(id);
        } catch (DirectorNotFoundException e) {
            log.debug("Удаление режиссера c неверным id: {}", id);
            throw new DirectorNotFoundException("Режиссер с id " + id + " не существует");
        }
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public List<Directors> getFilmDirectors(int filmId) {
        return jdbcTemplate.query(
                "select d.* from directors as d join director_films as df on d.id = df.director_id " +
                        "where df.film_id = ?",
                this::directorFromRow,
                filmId
        );
    }

    @Override
    public void addFilmDirectors(Film film, int filmId) {
        if (!getFilmDirectors(film.getId()).isEmpty()) {
            String sqlQuery = "delete from director_films where film_id = ?";
            jdbcTemplate.update(sqlQuery, filmId);
        }
        if (film.getDirectors() == null) {
            return;
        }
        String sqlQuery = "insert into director_films(film_id, director_id) " +
                "values (?, ?)";
        film.getDirectors().forEach(director -> jdbcTemplate.update(sqlQuery, filmId, director.getId()));
    }

    private Map<String, Object> directorToRow(Directors directors) {
        Map<String, Object> values = new HashMap<>();
        values.put("id", directors.getId());
        values.put("name", directors.getName());
        return values;
    }

    private Directors directorFromRow(ResultSet rs, int rowNum) throws SQLException {
        return new Directors(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}
