package ru.yandex.practicum.filmorate.exceptions;

/*
    Исключение при неверном вводе данных
 */
public class InvalidDataEnteredException extends RuntimeException {
    public InvalidDataEnteredException(String message) {
        super(message);
    }
}
