package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.IsAfter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
public class Film {
    private int id;
    @NotBlank(message = "Название фильма не может быть пустым")
    private final String name;
    @NotNull
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private final String description;
    @NotNull
    @Past(message = "Дата релиза не может быть в будущем")
    @IsAfter(current = "1895-12-28", message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private final LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private final int duration;
    @Valid
    private final RatingMPA mpa;
    @Valid
    private List<Genre> genres;
}
