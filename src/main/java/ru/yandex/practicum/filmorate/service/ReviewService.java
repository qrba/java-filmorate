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
        feedStorage.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType("REVIEW")
                .operation("ADD")
                .entityId(review.getReviewId())
                .timestamp(Instant.now())
                .build());
        return reviewStorage.add(review);
    }

    public Review update(Review review) {
        feedStorage.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType("REVIEW")
                .operation("UPDATE")
                .entityId(review.getReviewId())
                .timestamp(Instant.now())
                .build());;
        return reviewStorage.update(review);
    }

    public void delete(int id) {
        Review review = reviewStorage.getReviewById(id);
        reviewStorage.delete(id);
        feedStorage.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType("REVIEW")
                .operation("REMOVE")
                .entityId(review.getReviewId())
                .timestamp(Instant.now())
                .build());
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
