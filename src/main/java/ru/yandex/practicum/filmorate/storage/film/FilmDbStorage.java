package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.util.FilmorateMapper;

import java.util.List;

@Slf4j
@Component("databaseFilm")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getAll() {
        String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating on " +
                "f.mpa_id = mpa_rating.id";
        return jdbcTemplate.query(sqlQuery, FilmorateMapper::filmFromRow);
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        film.setId(simpleJdbcInsert.executeAndReturnKey(FilmorateMapper.filmToRow(film)).intValue());
        log.info("Добавлен новый фильм {}.", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "update films set name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? where id = ?";
        int filmId = film.getId();
        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                filmId
        );
        if (rowsUpdated == 1) {
            log.info("Обновлен фильм {}.", film);
            return film;
        } else {
            throw new FilmNotFoundException("Фильм с id=" + filmId + " не найден.");
        }
    }

    @Override
    public Film getFilmById(int id) {
        try {
            String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating " +
                    "on f.mpa_id = mpa_rating.id where f.id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, FilmorateMapper::filmFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильм с id=" + id + " не найден.");
        }
    }

    @Override
    public List<Film> getMostPopular(int size) {
        String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating on f.mpa_id " +
                "= mpa_rating.id left join film_likes on f.id = film_likes.film_id group by film_likes.film_id, f.id " +
                "order by count (film_likes.user_id) desc, film_likes.film_id limit ?";
        return jdbcTemplate.query(sqlQuery, FilmorateMapper::filmFromRow, size);
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("delete from films where id = ?", id);
        log.info("Удален фильм с id={}.", id);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sqlQuery = "select f.*, mr.name as mpa_name from films as f left join mpa_rating as mr " +
                "on f.mpa_id = mr.id left join film_likes as fl1 on fl1.film_id = f.id left join film_likes as fl2 " +
                "on fl2.film_id = f.id left join film_likes as fl3 on fl3.film_id = f.id " +
                "where fl1.user_id = ? and fl2.user_id = ? group by f.id order by count (fl3.user_id) desc, f.id";
        return jdbcTemplate.query(sqlQuery, FilmorateMapper::filmFromRow, userId, friendId);
    }
}