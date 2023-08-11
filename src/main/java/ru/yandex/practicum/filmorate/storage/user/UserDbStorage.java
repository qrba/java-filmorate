package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("databaseUser")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query("select * from users", this::userMapFromRow);
    }

    @Override
    public User add(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
        user.setId(simpleJdbcInsert.executeAndReturnKey(userMapToRow(user)).intValue());
        log.info("Добавлен новый пользователь {}.", user);
        return user;
    }

    @Override
    public User update(User user) {
        String sqlQuery = "update users set email = ?, login = ?, name = ?, birthday = ? where id = ?";
        int userId = user.getId();
        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                userId
        );
        if (rowsUpdated == 1) {
            log.info("Обновлен пользователь {}.", user);
            return user;
        } else {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден.");
        }
    }

    @Override
    public User getUserById(int id) {
        try {
            return jdbcTemplate.queryForObject("select * from users where id = ?", this::userMapFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователь с id=" + id + " не найден.");
        }
    }

    @Override
    public void addFriend(int id, int friendId) {
        getUserById(id);
        getUserById(friendId);
        String sqlQuery = "merge into friends (user_id, friend_id) key (user_id, friend_id) values (?, ?)";
        jdbcTemplate.update(sqlQuery, id, friendId);
        log.info("Пользователь с id={} добавил в друзья пользователя с id={}.", id, friendId);
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        String sqlQuery = "delete from friends where user_id = ? and friend_id = ?";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public List<User> getFriends(int id) {
        getUserById(id);
        return jdbcTemplate.query("select users.* from users join friends on users.id = friends.friend_id " +
                "where friends.user_id = ?", this::userMapFromRow, id);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        getUserById(id);
        getUserById(otherId);
        return jdbcTemplate.query("select users.* from users join friends on users.id = friends.friend_id " +
                "where friends.user_id in (?, ?) group by users.id having count (friends.friend_id) > 1",
                this::userMapFromRow, id, otherId);
    }

    private User userMapFromRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                LocalDate.parse(rs.getString("birthday"))
        );
    }

    private Map<String, Object> userMapToRow(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }
}
