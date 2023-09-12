package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FilmorateMapper {
    public static Map<String, Object> filmToRow(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("mpa_id", film.getMpa().getId());
        return values;
    }

    public static Film filmFromRow(ResultSet rsFilm, int rowNumFilm) throws SQLException {
        Film film = new Film(
                rsFilm.getString("name"),
                rsFilm.getString("description"),
                LocalDate.parse(rsFilm.getString("release_date")),
                rsFilm.getInt("duration"),
                new RatingMPA(rsFilm.getInt("mpa_id"), rsFilm.getString("mpa_name"))
        );
        film.setId(rsFilm.getInt("id"));
        return film;
    }

    public static Map<String, Object> userToRow(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }

    public static User userFromRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                LocalDate.parse(rs.getString("birthday"))
        );
    }

    public static Genre genreFromRow(ResultSet rs, int rowNum)throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }

    public static Map<String, Object> reviewToRow(Review review) {
        Map<String, Object> values = new HashMap<>();
        values.put("content", review.getContent());
        values.put("is_positive", review.getIsPositive());
        values.put("user_id", review.getUserId());
        values.put("film_id", review.getFilmId());
        return values;
    }

    public static Review reviewFromRow(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review(
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getInt("user_id"),
                rs.getInt("film_id"),
                rs.getInt("useful")
        );
        review.setReviewId(rs.getInt("id"));
        return review;
    }
}
