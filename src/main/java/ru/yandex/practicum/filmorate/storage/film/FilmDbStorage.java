package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.InvalidDataEnteredException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.RatingMPAStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
                .releaseDate(LocalDate.parse(rsFilm.getString("release_date")))
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

    /*
        Поиск по названию фильма или по режиссеру
     */
    @Override
    public List<Film> search(String textForSearch, String searchParameter) {
        String[] splitSearchParameter = searchParameter.split(",");
        validateText(splitSearchParameter);
        if (splitSearchParameter.length == 2) {
            String sqlQuery =
                    "SELECT COUNT (fl.film_id) AS rate, f.*, d.name, mr.name AS mpa_name, g.*, d.* " +
                            "FROM films AS f " +
                            "LEFT JOIN film_likes AS fl ON fl.film_id=f.id " +
                            "LEFT JOIN director_films AS df ON df.film_id=f.id " +
                            "LEFT JOIN directors AS d ON d.id=df.director_id " +
                            "LEFT JOIN mpa_rating AS mr ON mr.id=f.mpa_id " +
                            "LEFT JOIN film_genres AS fg ON fg.film_id=f.id " +
                            "LEFT JOIN genres AS g ON fg.genre_id=g.id " +
                            "WHERE d.name ILIKE ? OR f.name ILIKE ? " +
                            "GROUP BY f.id, d.name, mr.id, g.id, d.id " +
                            "ORDER BY rate DESC";

            return jdbcTemplate.query(sqlQuery, this::filmFromRow, "%" + textForSearch + "%", "%" + textForSearch + "%");
        } else {
            String sqlQuery = getSqlQuery(searchParameter);
            return jdbcTemplate.query(sqlQuery, this::filmFromRow, "%" + textForSearch + "%");
        }
    }

    /*
        Определение параметра для фильтрации
     */
    private String getSqlQuery(String searchParameter) {
        String condition;
        if (searchParameter.equals("director")) {
            condition = "WHERE d.name ILIKE ? ";
        } else {
            condition = "WHERE f.name ILIKE ? ";
        }
        String sqlQuery = "SELECT COUNT (fl.film_id) AS rate, f.*, d.name, mr.name AS mpa_name, g.*, d.* " +
                "FROM films AS f " +
                "LEFT JOIN film_likes AS fl ON fl.film_id=f.id " +
                "LEFT JOIN director_films AS df ON df.film_id=f.id " +
                "LEFT JOIN directors AS d ON d.id=df.director_id " +
                "LEFT JOIN mpa_rating AS mr ON mr.id=f.mpa_id " +
                "LEFT JOIN film_genres AS fg ON fg.film_id=f.id " +
                "LEFT JOIN genres AS g ON fg.genre_id=g.id " +
                condition +
                "GROUP BY f.id, d.name, mr.id, g.id, d.id " +
                "ORDER BY rate DESC";
        return sqlQuery;
    }

    /*
        Проверка введенных аргументов для поиска
     */
    private void validateText(String[] text) {
        if (text.length > 3 || text.length == 0) {
            throw new InvalidDataEnteredException("В запросе введено не корректное количество аргументов");
        }
        if (!(text[0].equals("director") || text[0].equals("title"))) {
            throw new InvalidDataEnteredException("В запросе написаны неверные параметры поиска");
        }
        if (text.length == 1) {
            return;
        }
        if (text[0].equals(text[1])) {
            throw new InvalidDataEnteredException("В запросе одинаковые аргументы");
        }
        if (!(text[1].equals("director") || text[1].equals("title"))) {
            throw new InvalidDataEnteredException("В запросе написаны неверные параметры поиска");
        }
    }
}