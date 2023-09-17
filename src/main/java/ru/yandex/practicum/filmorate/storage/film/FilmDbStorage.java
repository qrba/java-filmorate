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
