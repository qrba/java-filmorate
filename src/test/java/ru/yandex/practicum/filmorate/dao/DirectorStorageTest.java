package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorStorageTest {
    private final DirectorStorage directorStorage;
    @Test
    void shouldGetAll() {
        Director director1 = new Director(0, "Test Name1");
        Director director2 = new Director(0, "Test Name2");

        directorStorage.addDirector(director1);
        directorStorage.addDirector(director2);

        assertEquals(2, directorStorage.getAll().size());
    }

    @Test
    void shouldAdd() {
        Director director1 = new Director(1, "Test Name1");
        assertEquals(0, directorStorage.getAll().size());
        directorStorage.addDirector(director1);

        assertEquals(director1, directorStorage.getDirectorById(1));
    }

    @Test
    void shouldUpdate() {
        Director director1 = new Director(1, "Test Name1");
        directorStorage.addDirector(director1);

        assertEquals(director1, directorStorage.getDirectorById(1));

        Director director2 = new Director(1, "Test Name2");
        directorStorage.updateDirector(director2);

        assertEquals(director2, directorStorage.getDirectorById(1));
    }

    @Test
    void shouldGetById() {
        Director director1 = new Director(1, "Test Name1");
        directorStorage.addDirector(director1);

        assertEquals(director1, directorStorage.getDirectorById(1));
    }

    @Test
    void shouldDeleteFilm() {
        Director director1 = new Director(1, "Test Name1");
        directorStorage.addDirector(director1);
        assertEquals(1, directorStorage.getAll().size());

        directorStorage.deleteDirector(1);
        assertEquals(0, directorStorage.getAll().size());
    }
}
