package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addLike(int filmId, int userId) {
        String sqlQuery = "merge into film_likes (film_id, user_id) key (film_id, user_id) values (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        log.info("Фильм с id={} получил лайк от пользователя с id={}.", filmId, userId);
        addEventAddLike(userId, filmId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        jdbcTemplate.update("delete from film_likes where film_id = ? and user_id = ?", filmId, userId);
        log.info("Удален лайк фильма с id={} от пользователя с id={}.", filmId, userId);
        addEventDeleteLike(userId, filmId);
    }

    @Override
    public List<Integer> getLikes(int filmId) {
        String sqlQuery = "select user_id from film_likes where film_id = ?";
        return jdbcTemplate.query(
                sqlQuery,
                (rs, rowNum) -> rs.getInt("user_id"),
                filmId);
    }

    private void addEventDeleteLike(Integer userId, Integer filmId) {
        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                "VALUES (?,?, 'LIKE', 'REMOVE', ?)";
        jdbcTemplate.update(sql, userId, Date.from(Instant.now()), filmId);
    }

    private void addEventAddLike(Integer userId, Integer filmId) {
        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                " VALUES (?,?, 'LIKE', 'ADD', ?)";
        jdbcTemplate.update(sql, userId, Date.from(Instant.now()), filmId);
    }
}
