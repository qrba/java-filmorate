package ru.yandex.practicum.filmorate.storage.feed;

public interface FeedStorage {
    void addEvent(int userId, String eventType, String operation, int entityId);
}
