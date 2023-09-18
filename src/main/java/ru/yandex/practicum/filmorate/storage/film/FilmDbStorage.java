package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.RatingMPAStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("databaseFilm")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RatingMPAStorage ratingMPADbStorage;
    private final GenreStorage genreDbStorage;
    private final DirectorStorage directorDbStorage;

    @Override
    public List<Film> getAll() {
        String sqlQuery = "select * from films";
        return jdbcTemplate.query(sqlQuery, this::filmFromRow);
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        int id = simpleJdbcInsert.executeAndReturnKey(filmToRow(film)).intValue();
        directorDbStorage.addFilmDirectors(film, id);
        genreDbStorage.addFilmGenres(film, id);
        log.info("Добавлен новый фильм {}.", getFilmById(id));
        return getFilmById(id);
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "update films set name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? where id = ?";
        try {
            getFilmById(film.getId());
            jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());
            genreDbStorage.addFilmGenres(film, film.getId());
            directorDbStorage.addFilmDirectors(film, film.getId());
            log.info("Обновлен фильм {}.", getFilmById(film.getId()));
            return getFilmById(film.getId());
        } catch (FilmNotFoundException e) {
            log.debug("Обновление фильма c неверным id: {}", film.getId());
            throw new FilmNotFoundException("Фильм с id " + film.getId() + " не существует");
        }
    }

    @Override
    public Film getFilmById(int id) {
        String sqlQuery = "select * from films where id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::filmFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Получение фильма с неверным id: {}", id);
            throw new FilmNotFoundException("Фильм с id " + id + " не существует");
        }
    }

    @Override
    public List<Film> getMostPopular(int size) {
        String sqlQuery = "select f.* from films as f left join film_likes as l on f.id = l.film_id " +
                "group by f.id order by count(l.film_id) desc limit ?";
        return jdbcTemplate.query(sqlQuery, this::filmFromRow, size);
    }

    @Override
    public List<Film> getDirectorsFilms(int directorId, String sortBy) {
        String sqlQuerySortedByYear = "select * from films as f where f.id in (select film_id from director_films " +
                "where director_id = ?) order by extract(year from cast(f.release_date as date))";

        String sqlQuerySortedByLikes = "select * from films as f left join film_likes as l on f.id = l.film_id " +
                "where f.id in (select film_id from director_films where " +
                "director_id = ?) group by f.id order by count(l.film_id)";

        if (sortBy.equals("year")) {
            return jdbcTemplate.query(sqlQuerySortedByYear, this::filmFromRow, directorId);
        } else {
            return jdbcTemplate.query(sqlQuerySortedByLikes, this::filmFromRow, directorId);
        }
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("delete from films where id = ?", id);
        log.info("Удален фильм с id={}.", id);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sqlQuery = "select f.* from films as f " +
                "left join film_likes as fl1 on fl1.film_id = f.id left join film_likes as fl2 " +
                "on fl2.film_id = f.id left join film_likes as fl3 on fl3.film_id = f.id " +
                "where fl1.user_id = ? and fl2.user_id = ? group by f.id order by count (fl3.user_id) desc, f.id";
        return jdbcTemplate.query(sqlQuery, this::filmFromRow, userId, friendId);
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        String sqlQuery = "select * from films where id in (select film_id from film_likes where user_id in " +
                "(select user_id from (select user_id, count(user_id) as c from film_likes where film_id in " +
                "(select film_id from film_likes where user_id = ?) " +
                "and user_id != ? group by user_id order by c limit 1)) " +
                "and film_id not in (select film_id from film_likes where user_id = ?)) ";
        return jdbcTemplate.query(sqlQuery, this::filmFromRow, userId, userId, userId);
    }

    private Film filmFromRow(ResultSet rsFilm, int rowNumFilm) throws SQLException {
        return Film.builder()
                .id(rsFilm.getInt("id"))
                .name(rsFilm.getString("name"))
                .description(rsFilm.getString("description"))
                .releaseDate(rsFilm.getDate("release_date").toLocalDate())
                .duration(rsFilm.getInt("duration"))
                .mpa(ratingMPADbStorage.getRatingMPAById(rsFilm.getInt("mpa_id")))
                .genres(genreDbStorage.getFilmGenres(rsFilm.getInt("id")))
                .directors(directorDbStorage.getFilmDirectors(rsFilm.getInt("id")))
                .build();
    }

    private Map<String, Object> filmToRow(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("mpa_id", film.getMpa().getId());
        return values;
    }
}