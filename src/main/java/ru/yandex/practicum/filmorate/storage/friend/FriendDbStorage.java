package ru.yandex.practicum.filmorate.storage.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.FilmorateMapper;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendDbStorage implements FriendStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(int userId, int friendId) {
        String sqlQuery = "merge into friends (user_id, friend_id) key (user_id, friend_id) values (?, ?)";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Пользователь с id={} добавил в друзья пользователя с id={}.", userId, friendId);
        addEventAddFriend(userId, friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        String sqlQuery = "delete from friends where user_id = ? and friend_id = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Пользователь с id={} удалил из друзей пользователя с id={}.", userId, friendId);
        addEventDeleteFriend(userId, friendId);
    }

    @Override
    public List<User> getFriends(int id) {
        return jdbcTemplate.query("select users.* from users join friends on users.id = friends.friend_id " +
                "where friends.user_id = ?", FilmorateMapper::userFromRow, id);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        return jdbcTemplate.query("select users.* from users join friends on users.id = friends.friend_id " +
                        "where friends.user_id in (?, ?) group by users.id having count (friends.friend_id) > 1",
                FilmorateMapper::userFromRow, id, otherId);
    }

    private void addEventDeleteFriend(Integer userId, Integer friendId) {
        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                "VALUES (?,?, 'FRIEND', 'REMOVE', ?)";
        jdbcTemplate.update(sql, userId, Date.from(Instant.now()), friendId);
    }

    private void addEventAddFriend(Integer userId, Integer friendId) {
        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                " VALUES (?,?, 'FRIEND', 'ADD',?)";
        jdbcTemplate.update(sql, userId, Date.from(Instant.now()), friendId);
    }
}
