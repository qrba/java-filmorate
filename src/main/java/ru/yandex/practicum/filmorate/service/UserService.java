package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("databaseUser")
    private final UserStorage storage;
    private final FilmService filmService;
    private final FeedStorage feedStorage;

    public List<User> getUsers() {
        return storage.getAll();
    }

    public User getUserById(int id) {
        return storage.getUserById(id);
    }

    public User add(User user) {
        return storage.add(user);
    }

    public User update(User user) {
        return storage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        storage.addFriend(userId, friendId);
        feedStorage.addEvent(userId, "FRIEND", "ADD",friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        storage.deleteFriend(userId, friendId);
        feedStorage.addEvent(userId, "FRIEND", "REMOVE",friendId);
    }

    public List<User> getFriends(int id) {
        getUserById(id);
        return storage.getFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        getUserById(id);
        getUserById(otherId);
        return storage.getCommonFriends(id, otherId);
    }

    public void delete(int id) {
        storage.delete(id);
    }

    public List<Film> getRecommendations(int userId) {
        storage.getUserById(userId);
        return filmService.getRecommendations(userId);
    }
   public List<Event> getUserFeed(int userId) {
        getUserById(userId);
        return feedStorage.getUserFeed(userId);
   }
}
