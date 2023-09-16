package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Data
public class Directors {
    @Positive
    private final int id;
    @Pattern(regexp = "^\\S.+|null")
    private final String name;
}
