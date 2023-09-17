package ru.yandex.practicum.filmorate.storage.reviewlikes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewLikesDbStorage implements ReviewLikesStorage {
    private final JdbcTemplate jdbcTemplate;

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
