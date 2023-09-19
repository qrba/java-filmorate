package ru.yandex.practicum.filmorate.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Builder
@Data
public class Event {
    @NotNull
    private Integer eventId;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
    @NotNull
    private final Integer userId;
    private String eventType;
    private String operation;
    @NotNull
    private final Integer entityId;
}