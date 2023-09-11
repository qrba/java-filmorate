package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private int id;
    @NotNull
    @Email(message = "Электронная почта не может быть пустой и должна содержать символ @")
    private final String email;
    @Pattern(regexp = "^\\S+$", message = "Логин не может быть пустым и содержать пробелы")
    private final String login;
    private String name;
    @NotNull
    @Past(message = "Дата рождения не может быть в будущем")
    private final LocalDate birthday;

    public String getName() {
        if (name == null || name.isBlank()) {
            name = this.login;
        }
        return name;
    }
}
