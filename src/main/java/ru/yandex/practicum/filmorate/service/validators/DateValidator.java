package ru.yandex.practicum.filmorate.service.validators;

import ru.yandex.practicum.filmorate.annotations.IsAfter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<IsAfter, LocalDate> {
    private String validDate;

    @Override
    public void initialize(IsAfter constraintAnnotation) {
        validDate = constraintAnnotation.current();
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        return date.isAfter(LocalDate.parse(validDate));
    }
}
