package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.FilmorateMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component("databaseUser")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query("select * from users", FilmorateMapper::userFromRow);
    }

    @Override
    public User add(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
        user.setId(simpleJdbcInsert.executeAndReturnKey(FilmorateMapper.userToRow(user)).intValue());
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
            return jdbcTemplate.queryForObject("select * from users where id = ?", FilmorateMapper::userFromRow, id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователь с id=" + id + " не найден.");
        }
    }

    private Feed createFeed(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .userId(rs.getInt("user_id"))
                .eventType(rs.getString("event_type"))
                .operation(rs.getString("operation"))
                .eventId(rs.getInt("id"))
                .entityId(rs.getInt("entity_id"))
                .timestamp(rs.getTimestamp("timestamp"))
                .build();
    }

    @Override
    public List<Feed> getUserFeed(Integer userId) {
        String sqlQuery = "SELECT * FROM events WHERE user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::createFeed, userId);
    }
}
