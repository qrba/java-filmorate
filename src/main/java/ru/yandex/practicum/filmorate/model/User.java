package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    @NotNull
    @Past
    private final LocalDate birthday;
    private final Set<Integer> friends = new HashSet<>();

    public void addFriend(int id) {
        friends.add(id);
    }

    public void deleteFriend(int id) {
        friends.remove(id);
    }

    public Set<Integer> getFriends() {
        return friends;
    }
}
