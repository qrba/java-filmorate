package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component("memoryFilm")
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films;
    private int idCounter;

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film add(Film film) {
        idCounter++;
        film.setId(idCounter);
        films.put(idCounter, film);
        log.info("Добавлен фильм {}.", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        int id = film.getId();
        if (films.get(id) == null) throw new FilmNotFoundException("Фильм с id=" + id + " не найден.");
        films.put(id, film);
        log.info("Обновлен фильм {}.", film);
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        Film film = films.get(id);
        if (film == null) throw new FilmNotFoundException("Фильм с id=" + id + " не найден.");
        return film;
    }

    @Override
    public void addLike(int id, int userId) {
        Film film = getFilmById(id);
        film.addLike(userId);
    }

    @Override
    public void deleteLike(int id, int userId) {
        Film film = getFilmById(id);
        film.deleteLike(userId);
    }

    @Override
    public List<Film> getMostPopular(int size) {
        return getAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikesNumber).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }
}
