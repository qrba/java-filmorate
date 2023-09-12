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

    @Override
    public void addLike(int reviewId, int userId, boolean hasLiked) {
        String sqlQuery = "merge into review_likes (review_id, user_id, liked) key (review_id, user_id) values (?, ?, ?)";
        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                reviewId,
                userId,
                hasLiked
        );
        if (rowsUpdated == 1) {
            log.info("Пользователь с id={} оценил отзыв с id={}.", userId, reviewId);
        } else {
            throw new ReviewNotFoundException("Отзыв с id=" + reviewId + " не найден.");
        }
    }

    @Override
    public void deleteLike(int reviewId, int userId, boolean liked) {
        String sqlQuery = "delete from review_likes where review_id = ? and user_id = ? and liked = ?";
        jdbcTemplate.update(sqlQuery, reviewId, userId, liked);
        log.info("Удалена оценка пользователя с id={} отзыву с id={}.", userId, reviewId);
    }
}
