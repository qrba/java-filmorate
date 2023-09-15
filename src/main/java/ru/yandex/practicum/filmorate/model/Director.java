package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
public class Director {
    @Positive
    private final int id;
    @NotBlank(message = "Имя режиссера не может быть пустым")
    private final String name;
}
