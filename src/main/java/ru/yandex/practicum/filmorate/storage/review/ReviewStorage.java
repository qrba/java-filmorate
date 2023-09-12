package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review add(Review review);

    Review update(Review review);

    void delete(int id);

    Review getReviewById(int id);

    List<Review> getSomeReviews(int filmId, int count);

    void addLike(int reviewId, int userId, boolean liked);

    void deleteLike(int reviewId, int userId, boolean liked);
}
