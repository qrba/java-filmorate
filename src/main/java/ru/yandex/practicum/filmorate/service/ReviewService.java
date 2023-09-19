package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.reviewlikes.ReviewLikesStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final ReviewLikesStorage reviewLikesStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    public Review add(Review review) {
        filmStorage.getFilmById(review.getFilmId());
        userStorage.getUserById(review.getUserId());
        Review addedReview = reviewStorage.add(review);
        feedStorage.addEvent(Event.builder()
                .userId(addedReview.getUserId())
                .eventType("REVIEW")
                .operation("ADD")
                .entityId(review.getReviewId())
                .timestamp(Date.from(Instant.now()))
                .build());
        return addedReview;
    }

    public Review update(Review review) {
        Review addedReview = reviewStorage.update(review);
        feedStorage.addEvent(Event.builder()
                .userId(addedReview.getUserId())
                .eventType("REVIEW")
                .operation("UPDATE")
                .entityId(addedReview.getReviewId())
                .timestamp(Date.from(Instant.now()))
                .build());
        return addedReview;
    }

    public void delete(int id) {
        Review review = reviewStorage.getReviewById(id);
        feedStorage.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType("REVIEW")
                .operation("REMOVE")
                .entityId(review.getReviewId())
                .timestamp(Date.from(Instant.now()))
                .build());
        reviewStorage.delete(id);
    }

    public Review getReviewById(int id) {
        return reviewStorage.getReviewById(id);
    }

    public List<Review> getReviewByFilmId(int filmId, int count) {
        if (filmId != 0) filmStorage.getFilmById(filmId);
        return reviewStorage.getSomeReviews(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        userStorage.getUserById(userId);
        reviewLikesStorage.addLike(reviewId, userId, true);
    }

    public void addDislike(int reviewId, int userId) {
        userStorage.getUserById(userId);
        reviewLikesStorage.addLike(reviewId, userId, false);
    }

    public void deleteLike(int reviewId, int userId) {
        userStorage.getUserById(userId);
        reviewLikesStorage.deleteLike(reviewId, userId, true);
    }

    public void deleteDislike(int reviewId, int userId) {
        userStorage.getUserById(userId);
        reviewLikesStorage.deleteLike(reviewId, userId, false);
    }
}
