package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorDbStorage;

    public List<Director> getAll() {
        return directorDbStorage.getAll();
    }

    public Director getDirectorById(int id) {
        return directorDbStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        return directorDbStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorDbStorage.updateDirector(director);
    }

    public void deleteDirector(int id) {
        directorDbStorage.deleteDirector(id);
    }
}
