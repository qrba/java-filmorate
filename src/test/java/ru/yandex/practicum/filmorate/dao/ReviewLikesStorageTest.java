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
import java.util.Collections;

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
        Film filmToAdd = Film.builder()
                .name("Film")
                .description("Film is a test entity")
                .releaseDate(LocalDate.parse("1985-10-20"))
                .duration(90)
                .mpa(new RatingMPA(1, "G"))
                .genres(Collections.emptyList())
                .directors(Collections.emptyList())
                .build();
        filmStorage.add(filmToAdd);
        User userToAdd = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("testUsername")
                .birthday(LocalDate.parse("2000-05-25"))
                .build();
        userStorage.add(userToAdd);
    }


    @Test
    void shouldAddLike() {
        Review review = Review.builder()
                .content("Review")
                .isPositive(true)
                .filmId(1)
                .userId(1)
                .build();
        reviewStorage.add(review);
        reviewLikesStorage.addLike(review.getReviewId(), 1, true);
        review = reviewStorage.getReviewById(1);

        assertEquals(1, review.getUseful());
    }

    @Test
    void shouldDeleteLike() {
        Review review = Review.builder()
                .content("Review")
                .isPositive(true)
                .filmId(1)
                .userId(1)
                .build();
        reviewStorage.add(review);
        reviewLikesStorage.addLike(review.getReviewId(), 1, true);
        reviewLikesStorage.deleteLike(1, 1, true);
        review = reviewStorage.getReviewById(1);

        assertEquals(0, review.getUseful());
    }
}
