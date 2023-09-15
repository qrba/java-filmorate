package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.util.FilmorateMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAll() {
        String sqlQuery = "select * from directors";
        return jdbcTemplate.query(
                sqlQuery,
                FilmorateMapper::directorFromRow
        );
    }

    @Override
    public Director getDirectorById(int id) {
        try {
            String sqlQuery = "select * from directors where id = ?";
            return jdbcTemplate.queryForObject(
                    sqlQuery,
                    FilmorateMapper::directorFromRow,
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DirectorNotFoundException("Режиссер с id=" + id + " не найден.");
        }
    }

    @Override
    public Director addDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");
        int id = simpleJdbcInsert.executeAndReturnKey(FilmorateMapper.directorToRow(director)).intValue();
        log.info("Добавлен новый режиссер {}.", getDirectorById(id));
        return getDirectorById(id);
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "update directors set id = ?, name = ?";
        try {
            getDirectorById(director.getId());
        } catch (DirectorNotFoundException e) {
            log.debug("Обновление режиссера c неверным id: {}", director.getId());
            throw new DirectorNotFoundException("Режиссер с id " + director.getId() + " не существует");
        }
        jdbcTemplate.update(sqlQuery,
                director.getId(),
                director.getName());
        return getDirectorById(director.getId());
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
    public List<Director> getFilmDirectors(int filmId) {
        return jdbcTemplate.query(
                "select d.* from directors as d join director_films as df on d.id = df.director_id " +
                        "where df.film_id = ?",
                FilmorateMapper::directorFromRow,
                filmId
        );
    }

    @Override
    public void addFilmDirectors(Film film) {
        if (!getFilmDirectors(film.getId()).isEmpty()) {
            String sqlQuery = "delete from director_films where film_id = ?";
            jdbcTemplate.update(sqlQuery, film.getId());
        }
        if (film.getDirector() == null) {
            return;
        }
        String sqlQuery = "insert into director_films(film_id, director_id) " +
                "values (?, ?)";
        film.getDirector().forEach(director -> jdbcTemplate.update(sqlQuery, film.getId(), director.getId()));
    }
}
