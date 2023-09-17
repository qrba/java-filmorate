package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewStorageTest {
    private final ReviewStorage reviewStorage;
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
    void shouldAddReview() {
        Review review = Review.builder()
                .content("Review")
                .isPositive(true)
                .filmId(1)
                .userId(1)
                .build();
        reviewStorage.add(review);
        Review addedReview = reviewStorage.add(review);

        assertEquals(review, addedReview);
    }

    @Test
    void shouldUpdateReview() {
        Review review = Review.builder()
                .content("Review")
                .isPositive(true)
                .filmId(1)
                .userId(1)
                .build();
        reviewStorage.add(review);
        review = Review.builder()
                .content("Updated review")
                .isPositive(false)
                .filmId(1)
                .userId(1)
                .build();
        review.setReviewId(1);
        Review updatedReview = reviewStorage.update(review);

        assertEquals(review, updatedReview);
    }

    @Test
    void shouldNotUpdateReviewWhenIncorrectId() {
        ReviewNotFoundException e = Assertions.assertThrows(
                ReviewNotFoundException.class,
                () -> {
                    Review review = Review.builder()
                            .content("Review")
                            .isPositive(true)
                            .filmId(1)
                            .userId(1)
                            .build();
                    reviewStorage.add(review);
                    review = Review.builder()
                            .content("Updated review")
                            .isPositive(false)
                            .filmId(1)
                            .userId(1)
                            .build();
                    reviewStorage.update(review);
                }
        );

        assertEquals("Отзыв с id=0 не найден.", e.getMessage());
    }

    @Test
    void shouldDeleteReview() {
        ReviewNotFoundException e = Assertions.assertThrows(
                ReviewNotFoundException.class,
                () -> {
                    Review review = Review.builder()
                            .content("Review")
                            .isPositive(true)
                            .filmId(1)
                            .userId(1)
                            .build();
                    reviewStorage.add(review);
                    int id = review.getReviewId();
                    reviewStorage.delete(id);
                    reviewStorage.getReviewById(id);
                }
        );

        assertEquals("Отзыв с id=1 не найден.", e.getMessage());
    }

    @Test
    void shouldGetReviewById() {
        Review review = Review.builder()
                .content("Review")
                .isPositive(true)
                .filmId(1)
                .userId(1)
                .build();
        reviewStorage.add(review);
        Review reviewById = reviewStorage.getReviewById(review.getReviewId());

        assertEquals(review, reviewById);
    }

    @Test
    void shouldNotGetReviewByIdWhenIncorrectId() {
        ReviewNotFoundException e = Assertions.assertThrows(
                ReviewNotFoundException.class,
                () -> {
                    Review review = Review.builder()
                            .content("Review")
                            .isPositive(true)
                            .filmId(1)
                            .userId(1)
                            .build();
                    reviewStorage.add(review);
                    reviewStorage.getReviewById(100);
                }
        );

        assertEquals("Отзыв с id=100 не найден.", e.getMessage());
    }

    @Test
    void shouldGetSomeReviews() {
        Review review = Review.builder()
                .content("Review")
                .isPositive(true)
                .filmId(1)
                .userId(1)
                .build();
        reviewStorage.add(review);
        List<Review> reviews = reviewStorage.getSomeReviews(1,1);

        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
    }
}
