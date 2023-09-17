package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LikeStorageTest {
    private final LikeStorage storage;

    @Test
    void shouldAddDeleteGetLikes(@Qualifier("databaseUser") UserStorage userStorage,
            @Qualifier("databaseFilm") FilmStorage filmStorage) {
        Film film = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .build();
        Film addedFilm = filmStorage.add(film);
        User user = User.builder()
                .id(1)
                .email("test@email.com")
                .login("testLogin")
                .name("testName")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        userStorage.add(user);
        storage.addLike(addedFilm.getId(), user.getId());
        List<Integer> likes = storage.getLikes(addedFilm.getId());

        assertEquals(List.of(user.getId()), likes);

        storage.deleteLike(film.getId(), user.getId());
        likes = storage.getLikes(film.getId());

        assertTrue(likes.isEmpty());
    }
}
