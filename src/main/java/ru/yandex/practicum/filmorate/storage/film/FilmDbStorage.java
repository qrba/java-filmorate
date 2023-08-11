package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component("databaseFilm")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> getAll() {
        String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating on " +
                "f.mpa_id = mpa_rating.id";
        return jdbcTemplate.query(sqlQuery, this::mapFromRow);
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        film.setId(simpleJdbcInsert.executeAndReturnKey(mapToRow(film)).intValue());
        log.info("Добавлен новый фильм {}.", film);
        return addFilmGenres(film);
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
            deleteFilmGenres(filmId);
            log.info("Обновлен фильм {}.", film);
            return addFilmGenres(film);
        } else {
            throw new FilmNotFoundException("Фильм с id=" + filmId + " не найден.");
        }
    }

    @Override
    public Film getFilmById(int id) {
        try {
            String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating " +
                    "on f.mpa_id = mpa_rating.id where f.id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильм с id=" + id + " не найден.");
        }
    }

    @Override
    public void addLike(int id, int userId) {
        getFilmById(id);
        String sqlQuery = "merge into film_likes (film_id, user_id) key (film_id, user_id) values (?, ?)";
        jdbcTemplate.update(sqlQuery, id, userId);
        log.info("Фильм с id={} получил лайк от пользователя с id={}.", id, userId);
    }

    @Override
    public void deleteLike(int id, int userId) {
        jdbcTemplate.update("delete from film_likes where film_id = ? and user_id = ?", id, userId);
    }

    @Override
    public List<Film> getMostPopular(int size) {
        String sqlQuery = "select f.*, mpa_rating.name as mpa_name from films as f left join mpa_rating on f.mpa_id " +
                "= mpa_rating.id left join film_likes on f.id = film_likes.film_id group by film_likes.film_id, f.id " +
                "order by count (film_likes.user_id) desc, film_likes.film_id limit ?";
        return jdbcTemplate.query(sqlQuery, this::mapFromRow, size);
    }

    private Film mapFromRow(ResultSet rsFilm, int rowNumFilm) throws SQLException {
        Film film = new Film(
                rsFilm.getString("name"),
                rsFilm.getString("description"),
                LocalDate.parse(rsFilm.getString("release_date")),
                rsFilm.getInt("duration"),
                new RatingMPA(rsFilm.getInt("mpa_id"), rsFilm.getString("mpa_name"))
        );
        film.setId(rsFilm.getInt("id"));
        film.setGenres(
            jdbcTemplate.query(
                "select g.* from genres as g join film_genres as fg on g.id = fg.genre_id where fg.film_id = ?",
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                film.getId()
            )
        );
        return film;
    }

    private Map<String, Object> mapToRow(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("mpa_id", film.getMpa().getId());
        return values;
    }

    private Film addFilmGenres(Film film) {
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

    private void deleteFilmGenres(int filmId) {
        jdbcTemplate.update("delete from film_genres where film_id = ?", filmId);
    }
}
