package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(this.reviewToRow(review)).intValue());
        log.info("Добавлен новый отзыв {}.", review);
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
            return getReviewById(id);
        } else {
            throw new ReviewNotFoundException("Отзыв с id=" + id + " не найден.");
        }
    }

    @Override
    public void delete(int id) {
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
            return jdbcTemplate.queryForObject(sqlQuery, this::reviewFromRow, id);
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
        return jdbcTemplate.query(sqlQuery, this::reviewFromRow, count);
    }

    private Map<String, Object> reviewToRow(Review review) {
        Map<String, Object> values = new HashMap<>();
        values.put("content", review.getContent());
        values.put("is_positive", review.getIsPositive());
        values.put("user_id", review.getUserId());
        values.put("film_id", review.getFilmId());
        return values;
    }

    private Review reviewFromRow(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getInt("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .useful(rs.getInt("useful"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .build();
    }
}
