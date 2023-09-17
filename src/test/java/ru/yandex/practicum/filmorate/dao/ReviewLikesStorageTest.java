package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.reviewlikes.ReviewLikesStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewLikesStorageTest {
    private final ReviewStorage reviewStorage;
    private final ReviewLikesStorage reviewLikesStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @BeforeEach
    void addUserAndFilm() {
        Film film = new Film("Film", "Film is a test entity",
                LocalDate.parse("1985-10-20"), 90, new RatingMPA(1, "G"));
        filmStorage.add(film);
        User user = new User(1, "test@email.com", "testLogin",
                "testName", LocalDate.parse("2000-05-25"));
        userStorage.add(user);
    }


    @Test
    void shouldAddLike() {
        Review review = new Review("Review", true, 1, 1);
        reviewStorage.add(review);
        reviewLikesStorage.addLike(review.getReviewId(), 1, true);
        review = reviewStorage.getReviewById(1);

        assertEquals(1, review.getUseful());
    }

    @Test
    void shouldDeleteLike() {
        Review review = new Review("Review", true, 1, 1);
        reviewStorage.add(review);
        reviewLikesStorage.addLike(review.getReviewId(), 1, true);
        reviewLikesStorage.deleteLike(1, 1, true);
        review = reviewStorage.getReviewById(1);

        assertEquals(0, review.getUseful());
    }
}
