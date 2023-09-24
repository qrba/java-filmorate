package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Event event) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("events")
                .usingGeneratedKeyColumns("id");
        simpleJdbcInsert.executeAndReturnKey(feedToRow(event)).intValue();
    }

    private Event rowToFeed(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .userId(rs.getInt("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(EventOperation.valueOf(rs.getString("operation")))
                .eventId(rs.getInt("id"))
                .entityId(rs.getInt("entity_id"))
                .timestamp(rs.getLong("timestamp"))
                .build();
    }

    private Map<String, Object> feedToRow(Event event) {
        Map<String, Object> values = new HashMap<>();
        values.put("timestamp", event.getTimestamp());
        values.put("user_id", event.getUserId());
        values.put("event_type", event.getEventType());
        values.put("operation", event.getOperation());
        values.put("entity_id", event.getEntityId());
        return values;
    }

    @Override
    public List<Event> getUserFeed(Integer userId) {
        String sqlQuery = "SELECT * FROM events WHERE user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::rowToFeed, userId);
    }
}
