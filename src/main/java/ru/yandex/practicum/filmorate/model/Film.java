package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.IsAfter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class Film {
    private int id = 0;
    @NotBlank
    private final String name;
    @Size(max = 200)
    private final String description;
    @IsAfter(current = "1895-12-28")
    private final LocalDate releaseDate;
    @Positive
    private final int duration;
}
