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

    /*
        Поиск по названию фильма или по режиссеру
     */
    @Override
    public List<Film> search(String textForSearch, String searchParameter) {
        String[] splitSearchParameter = searchParameter.split(",");
        validateText(splitSearchParameter);
        if (splitSearchParameter.length == 2) {
            String sqlQuery =
                    "SELECT COUNT (fl.film_id) AS rate, f.*, d.name " +
                            "FROM films AS f " +
                            "LEFT JOIN film_likes AS fl ON fl.film_id=f.id " +
                            "LEFT JOIN director_films AS df ON df.film_id=f.id " +
                            "LEFT JOIN directors AS d ON d.id=df.director_id " +
                            "WHERE d.name LIKE %?% OR f.name LIKE %?% " +
                            "GROUP BY f.id " +
                            "ORDER BY rate DESC";
            return jdbcTemplate.query(sqlQuery, FilmorateMapper::filmFromRow, textForSearch, textForSearch);
        } else {
            String sqlQuery = getSqlQuery(searchParameter);
            return jdbcTemplate.query(sqlQuery, FilmorateMapper::filmFromRow, textForSearch);
        }
    }

    /*
        Определение параметра для фильтрации
     */
    private String getSqlQuery(String searchParameter) {
        String condition;
        if (searchParameter.equals("director")) {
            condition = "WHERE d.name LIKE %?% ";
        } else {
            condition = "WHERE f.name LIKE %?% ";
        }
        String sqlQuery = "SELECT COUNT (fl.film_id) AS rate, f.*, d.name " +
                "FROM films AS f " +
                "LEFT JOIN film_likes AS fl ON fl.film_id=f.id " +
                "LEFT JOIN director_films AS df ON df.film_id=f.id " +
                "LEFT JOIN directors AS d ON d.id=df.director_id " +
                condition +
                "GROUP BY f.id " +
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
        if (text[0].equals(text[1])) {
            throw new InvalidDataEnteredException("В запросе одинаковые аргументы");
        }
        if (!((text[0].equals("director") || text[0].equals("title"))
                && (text[1].equals("director") || text[1].equals("title")))) {
            throw new InvalidDataEnteredException("В запросе написаны неверные параметры поиска");
        }
    }
}