package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService service;

    @GetMapping
    public Collection<Film> getFilms() {
        return service.getFilms();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return service.add(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return service.update(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return service.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        service.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable int id) {
        service.delete(id);
    }


    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return service.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getDirectorsFilms(@Valid @Positive @PathVariable int directorId,
                                        @Valid @Pattern(regexp = "year|likes") @RequestParam String sortBy) {
        return service.getDirectorsFilms(directorId, sortBy);
    }

    @GetMapping("/popular")
    public List<Film> getPopularsGenreAndYear(@RequestParam(value = "count", defaultValue = "10") int limit,
                                              @RequestParam(defaultValue = "-1") int genreId,
                                              @RequestParam(defaultValue = "-1") int year) {
        return service.getPopularsGenreAndYear(limit, genreId, year);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam("query") String query,
                                  @Valid @Pattern(regexp = "director|title|director,title|title,director")
                                  @RequestParam("by") String by) {
        return service.searchFilms(query, by);
    }
}
