package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.RatingMPAStorage;
import ru.yandex.practicum.filmorate.util.FilmorateMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component("databaseFilm")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorStorage directorStorage;
    private final RatingMPAStorage ratingMPADbStorage;
    private final GenreStorage genreDbStorage;
    private final DirectorStorage directorDbStorage;

    @Override
    public List<Film> getAll() {
        String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating on " +
                "f.mpa_id = mpa_rating.id";
        return jdbcTemplate.query(sqlQuery, this::filmFromRow);
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        film.setId(simpleJdbcInsert.executeAndReturnKey(FilmorateMapper.filmToRow(film)).intValue());
        directorStorage.addFilmDirectors(film);
        log.info("Добавлен новый фильм {}.", film);
        return getFilmById(film.getId());
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
            directorStorage.addFilmDirectors(film);
            log.info("Обновлен фильм {}.", film);
            return getFilmById(film.getId());
        } else {
            throw new FilmNotFoundException("Фильм с id=" + filmId + " не найден.");
        }
    }

    @Override
    public Film getFilmById(int id) {
        try {
            String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating " +
                    "on f.mpa_id = mpa_rating.id where f.id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, this::filmFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильм с id=" + id + " не найден.");
        }
    }

    @Override
    public List<Film> getMostPopular(int size) {
        String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating on f.mpa_id " +
                "= mpa_rating.id left join film_likes on f.id = film_likes.film_id group by film_likes.film_id, f.id " +
                "order by count (film_likes.user_id) desc, film_likes.film_id limit ?";
        return jdbcTemplate.query(sqlQuery, this::filmFromRow, size);
    }

    @Override
    public List<Film> getDirectorsFilms(int directorId, String sortBy) {
        String sqlQuerySortedByYear = "select * from films as f where f.id in (select id from director_films where " +
                "director_id = ?) order by extract(year from cast(f.release_date as date))";

        String sqlQuerySortedByLikes = "select * from films as f left join film_likes as l on f.id = l.film_id " +
                "where id in (select id from director_films where " +
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
                .releaseDate(LocalDate.parse(rsFilm.getString("release_date")))
                .duration(rsFilm.getInt("duration"))
                .mpa(ratingMPADbStorage.getRatingMPAById(rsFilm.getInt("mpa_id")))
                .genres(genreDbStorage.getFilmGenres(rsFilm.getInt("id")))
                .director(directorDbStorage.getFilmDirectors(rsFilm.getInt("id")))
                .build();
    }
}
