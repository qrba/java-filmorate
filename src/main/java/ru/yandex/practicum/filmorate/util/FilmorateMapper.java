package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FilmorateMapper {

    public static Map<String, Object> userToRow(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }

    public static User userFromRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                LocalDate.parse(rs.getString("birthday"))
        );
    }
}


/*
    1. Проблема с классом FilmService: каждое новое поле Film, которого нет в методе filmFromRow, придется добавлять в сервисе, что плохо
    Это создает проблемы и в дао-классах, и перегружает логику кода, и самое важное - инициализацию дополнительных полей приходится повторять в каждом методе.
    Решение - Собирать полностью готовый фильм со всеми полями в методе filmFromRow.
        Тогда при добавлении нового поля его инициализацию нужно будет добавить только в этот метод. Final-поля в классах тоже можно будет убрать
    Builder поможет сильно сократить код и решить проблемы десериализации, т.к. больше не нужны конструкторы. Иначе при добавлении нового поля придется
    постоянно лезть и менять все в каждом конструкторе + в тестах.
        Добавление записей в связанные таблицы на мой взгляд тоже лучше реализовать в дао-классах, а не в сервисах (Film -> Genres, Directors).
    Это уже не так критично, но код будет более структурированный - добавили фильм => сразу добавили его жанры, режиссеров.

    Чтобы это работало, нужно все методы из класса Util распихать по соответствующим классам.
    Как итог:

            a) вся логика по изменению сущностей ляжет только лишь на соответствующие этим сущностям дао-классы,
            что сильно упростит добавление нового функционала;
            b) функционал, не связанный с сущностями, а лишь фиксирующий работу с ними ляжет на сервисы (конфликтов при слиянии будет в разы меньше)

    2. Часть методов возвращает не полный объект Film - исправление в дао-классах решает проблему. Хорошо бы везде перепроверить, что
    методы при добавлении чего-то в БД возвращают результат именно из БД, а не тот объект, который приходит в метод!!!

 */
