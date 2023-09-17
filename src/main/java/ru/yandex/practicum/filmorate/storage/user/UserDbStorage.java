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
        return jdbcTemplate.query("select * from users", this::userFromRow);
    }

    @Override
    public User add(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
        int id = simpleJdbcInsert.executeAndReturnKey(userToRow(user)).intValue();
        log.info("Добавлен новый пользователь {}.", user);
        return getUserById(id);
    }

    @Override
    public User update(User user) {
        String sqlQuery = "update users set email = ?, login = ?, name = ?, birthday = ? where id = ?";
        try {
            getUserById(user.getId());
        } catch (UserNotFoundException e) {
            log.debug("Обновление пользователя c неверным id: {}", user.getId());
            throw new UserNotFoundException("Пользователь с id " + user.getId() + " не существует");
        }
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info("Обновлен пользователь {}.", user);
        return getUserById(user.getId());
    }

    @Override
    public User getUserById(int id) {
        String sqlQuery = "select * from users where id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::userFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Получение пользователя с неверным id: {}", id);
            throw new UserNotFoundException("Пользователь с id=" + id + " не найден.");
        }
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("delete from users where id = ?", id);
        log.info("Удален пользователь с id={}.", id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sqlQuery = "merge into friends (user_id, friend_id) key (user_id, friend_id) values (?, ?)";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Пользователь с id={} добавил в друзья пользователя с id={}.", userId, friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        String sqlQuery = "delete from friends where user_id = ? and friend_id = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Пользователь с id={} удалил из друзей пользователя с id={}.", userId, friendId);
    }

    @Override
    public List<User> getFriends(int id) {
        String sqlQuery = "select * from users where id in (select friend_id from friends where user_id = ?)";
        return jdbcTemplate.query(sqlQuery, this::userFromRow, id);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        String sqlQuery = "select * from users where id in " +
                "(select f.friend_id from friends f where f.user_id = ? AND f.friend_id in " +
                "(select f.friend_id from friends f where f.user_id = ?))";
        return jdbcTemplate.query(sqlQuery, this::userFromRow, id, otherId);
    }

    private User userFromRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }

    private Map<String, Object> userToRow(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }
}
