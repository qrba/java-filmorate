package ru.yandex.practicum.filmorate.dao;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.util.DateUtil.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeedStorageTest {
    private final FeedStorage feedStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private Film filmToAdd;
    private User userToAdd;
    private User user2ToAdd;

    @BeforeEach
    void addUserAndFilm() {
        filmToAdd = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        filmStorage.add(filmToAdd);
        userToAdd = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("testUsername")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        User addedUser = userStorage.add(userToAdd);
        userToAdd.setId(addedUser.getId());
        user2ToAdd = User.builder()
                .email("test2@email.com")
                .login("test2Login")
                .name("test2Username")
                .birthday(LocalDate.parse("2002-05-25"))
                .build();
        userStorage.add(user2ToAdd);
        User added2User = userStorage.add(user2ToAdd);
        user2ToAdd.setId(addedUser.getId());
    }

    @Test
    void shouldAddFriendAddEvent() {
        Event event = Event.builder()
                .eventId(1)
                .userId(userToAdd.getId())
                .eventType("LIKE")
                .operation("ADD")
                .entityId(user2ToAdd.getId())
                .timestamp(Instant.now())
                .build();
        feedStorage.addEvent(event);
        assertEquals(List.of(event), feedStorage.getUserFeed(1));
    }
}

