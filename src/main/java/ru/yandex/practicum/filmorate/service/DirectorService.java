package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Directors;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorDbStorage;

    public List<Directors> getAll() {
        return directorDbStorage.getAll();
    }

    public Directors getDirectorById(int id) {
        return directorDbStorage.getDirectorById(id);
    }

    public Directors addDirector(Directors directors) {
        return directorDbStorage.addDirector(directors);
    }

    public Directors updateDirector(Directors directors) {
        return directorDbStorage.updateDirector(directors);
    }

    public void deleteDirector(int id) {
        directorDbStorage.deleteDirector(id);
    }
}
