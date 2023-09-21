package ru.yandex.practicum.filmorate.model;


import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Builder
@Data
public class Event {
    @NotNull
    private Integer eventId;
    private final Long timestamp;
    @NotNull
    private final Integer userId;
    private final EventType eventType;
    private final EventOperation operation;
    @NotNull
    private final Integer entityId;
}