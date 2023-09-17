package ru.yandex.practicum.filmorate.storage.reviewlikes;

public interface ReviewLikesStorage {
    void addLike(int reviewId, int userId, boolean liked);

    void deleteLike(int reviewId, int userId, boolean liked);
}
