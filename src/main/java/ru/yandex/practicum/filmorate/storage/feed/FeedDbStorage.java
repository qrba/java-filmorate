package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(int userId, String eventType, String operation, int entityId) {
        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                "VALUES (?,?,?,?,?)";
        jdbcTemplate.update(sql, userId, Date.from(Instant.now()), eventType, operation, entityId);
    }
    private Event createFeed(ResultSet rs, int rowNum) throws SQLException {
            return Event.builder()
                    .userId(rs.getInt("user_id"))
                    .eventType(rs.getString("event_type"))
                    .operation(rs.getString("operation"))
                    .eventId(rs.getInt("id"))
                    .entityId(rs.getInt("entity_id"))
                    .timestamp(rs.getTimestamp("timestamp"))
                    .build();
        }
    @Override
    public List<Event> getUserFeed(Integer userId) {
        String sqlQuery = "SELECT * FROM events WHERE user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::createFeed, userId);
    }
}
