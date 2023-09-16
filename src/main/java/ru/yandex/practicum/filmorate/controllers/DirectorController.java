package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Directors;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Directors> getAll() {
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Directors getDirectorById(@Valid @Positive @PathVariable int id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Directors addDirector(@Valid @RequestBody Directors directors) {
        return directorService.addDirector(directors);
    }

    @PutMapping
    public Directors updateDirector(@Valid @RequestBody Directors directors) {
        return directorService.updateDirector(directors);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@Valid @Positive @PathVariable int id) {
        directorService.deleteDirector(id);
    }
}
