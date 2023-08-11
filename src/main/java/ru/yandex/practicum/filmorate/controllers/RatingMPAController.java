package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.service.RatingMPAService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingMPAController {
    private final RatingMPAService service;

    @GetMapping
    public List<RatingMPA> getRatingsMPA() {
        return service.getRatingsMPA();
    }

    @GetMapping("/{id}")
    public RatingMPA getRatingMPAById(@PathVariable int id) {
        return service.getRatingMPAById(id);
    }
}
