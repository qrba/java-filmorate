package ru.yandex.practicum.filmorate.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Date;

@Builder
@Data
public class Event {
    @NotNull
    private Integer eventId;
   // @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Instant timestamp;
    @NotNull
    private final Integer userId;
    private String eventType;
    private String operation;
    @NotNull
    private final Integer entityId;
}