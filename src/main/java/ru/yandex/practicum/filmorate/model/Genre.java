package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Positive;

@Data
public class Genre {
    @Positive
    private final int id;
    private final String name;
}
