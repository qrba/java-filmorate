package ru.yandex.practicum.filmorate.storage.like;

import java.util.List;

public interface LikeStorage {
    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    List<Integer> getLikes(int filmId);
}
