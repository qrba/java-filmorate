package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class User {
    private int id;
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
    @JsonIgnore
    private final Map<Integer, Boolean> friends = new HashMap<>();

    public void addFriend(int id) {
        friends.put(id, false);
    }

    public void deleteFriend(int id) {
        friends.remove(id);
    }

    public Set<Integer> getFriends() {
        return friends.keySet();
    }
}
