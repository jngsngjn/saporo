package project.saporo.dto;

import java.time.LocalDate;
import java.util.Objects;

public record DailyRate(LocalDate date, Double rate) {
    public DailyRate {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(rate, "rate must not be null");
    }
}