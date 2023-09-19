package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface FeedStorage {
    void addEvent(int userId, String eventType, String operation, int entityId);
    List<Event> getUserFeed(Integer userId);
}
