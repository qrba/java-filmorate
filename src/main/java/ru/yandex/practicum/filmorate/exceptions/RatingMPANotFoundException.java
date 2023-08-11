package ru.yandex.practicum.filmorate.exceptions;

public class RatingMPANotFoundException extends RuntimeException {
    public RatingMPANotFoundException(String message) {
        super(message);
    }
}