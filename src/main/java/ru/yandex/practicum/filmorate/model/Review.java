package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
@Data
public class Review {
    private int reviewId;
    @NotNull
    @NotBlank(message = "Текст обзора не может быть пустым")
    private final String content;
    @NotNull
    private final Boolean isPositive;
    @NotNull
    private final Integer userId;
    @NotNull
    private final Integer filmId;
    private int useful;
}
