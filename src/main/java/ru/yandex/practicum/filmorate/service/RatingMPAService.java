package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.mpa.RatingMPAStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingMPAService {
    @Qualifier("databaseMPA")
    private final RatingMPAStorage storage;

    public List<RatingMPA> getRatingsMPA() {
        return storage.getAll();
    }

    public RatingMPA getRatingMPAById(int id) {
        return storage.getRatingMPAById(id);
    }
}
