package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.util.List;

public interface RatingMPAStorage {
    List<RatingMPA> getAll();

    RatingMPA getRatingMPAById(int id);
}
