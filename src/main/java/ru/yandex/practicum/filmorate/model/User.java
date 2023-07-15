package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
public class User {
    private int id = 0;
    @NotNull
    @Email
    private final String email;
    @NotBlank
    @Pattern(regexp = "^\\S+$")
    private final String login;
    private String name;
    @Past
    private final LocalDate birthday;
}
