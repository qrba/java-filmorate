package ru.yandex.practicum.filmorate.exceptions;

public class ModelValidationException extends RuntimeException {
    public ModelValidationException(String message) {
        super(message);
    }
}
