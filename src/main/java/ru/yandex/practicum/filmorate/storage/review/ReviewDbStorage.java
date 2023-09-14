package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.util.FilmorateMapper;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review add(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("id");
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(FilmorateMapper.reviewToRow(review)).intValue());
        log.info("Добавлен новый отзыв {}.", review);
        addEventAddReview(review.getUserId(), review.getReviewId());
        return review;
    }

    @Override
    public Review update(Review review) {
        String sqlQuery = "update reviews set content = ?, is_positive = ? where id = ?";
        int id = review.getReviewId();
        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                id
        );
        if (rowsUpdated == 1) {
            log.info("Обновлен отзыв {}.", review);
            addEventReviewUpdate(review.getReviewId());
            return getReviewById(id);
        } else {
            throw new ReviewNotFoundException("Отзыв с id=" + id + " не найден.");
        }
    }

    @Override
    public void delete(int id) {
        Review review = getReviewById(id);
        addEventDeleteReview(review.getUserId(), id);
        String sqlQuery = "delete from reviews where id = ?";
        jdbcTemplate.update(sqlQuery, id);
        log.info("Удален отзыв с id={}.", id);

    }

    @Override
    public Review getReviewById(int id) {
        try {
            String sqlQuery = "select r.*, " +
                    "sum (case when rl.liked = true then 1 when rl.liked = false then -1 else 0 end) as useful from " +
                    "reviews as r left join review_likes as rl on r.id = rl.review_id where r.id = ? group by r.id";
            return jdbcTemplate.queryForObject(sqlQuery, FilmorateMapper::reviewFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReviewNotFoundException("Отзыв с id=" + id + " не найден.");
        }
    }

    @Override
    public List<Review> getSomeReviews(int filmId, int count) {
        String where = "";
        if (filmId != 0) where = "where r.film_id = " + filmId;
        String sqlQuery = "select r.*, " +
                "sum (case when rl.liked = true then 1 when rl.liked = false then -1 else 0 end) as useful " +
                "from reviews as r left join review_likes as rl on r.id = rl.review_id "
                + where + " group by r.id order by useful desc limit ?";
        return jdbcTemplate.query(sqlQuery, FilmorateMapper::reviewFromRow, count);
    }

    private void addEventDeleteReview(Integer userId, Integer reviewId) {
        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                "VALUES (?,?, 'REVIEW', 'REMOVE', ?)";
        jdbcTemplate.update(sql, userId, Date.from(Instant.now()), reviewId);
    }

    private void addEventAddReview(Integer userId, Integer reviewId) {
        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                " VALUES (?,?, 'REVIEW', 'ADD', ?)";
        jdbcTemplate.update(sql, userId, Date.from(Instant.now()), reviewId);
    }

    private void addEventReviewUpdate(Integer reviewId) {
        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                " VALUES (?, ?, 'REVIEW', 'UPDATE', ?)";
        Review review = getReviewById(reviewId);
        jdbcTemplate.update(sql, review.getUserId(), Date.from(Instant.now()), reviewId);
    }
}
