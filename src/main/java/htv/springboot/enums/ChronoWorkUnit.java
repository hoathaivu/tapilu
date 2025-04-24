package htv.springboot.enums;

import lombok.Getter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public enum ChronoWorkUnit {
    SECONDS(ChronoUnit.SECONDS, Duration.ofSeconds(1)),
    MINUTES(ChronoUnit.MINUTES, Duration.ofSeconds(60)),
    HOURS(ChronoUnit.HOURS, Duration.ofSeconds(60 * 60)),
    //The estimated number of working hours in a day is 8
    DAYS(ChronoUnit.DAYS, Duration.ofSeconds(8 * 60 * 60)),
    //The estimated number of working days in a week is 5
    WEEKS(ChronoUnit.WEEKS, Duration.ofSeconds(5 * 8 * 60 * 60)),
    MONTHS(ChronoUnit.MONTHS, Duration.ofSeconds((261 * 8 * 60 * 60) / 12)),
    //The estimated number of working days in a year is 261
    YEARS(ChronoUnit.YEARS, Duration.ofSeconds(261 * 8 * 60 * 60));

    @Getter
    private final ChronoUnit chronoUnit;
    @Getter
    private final Duration duration;

    ChronoWorkUnit(ChronoUnit chronoUnit, Duration estimatedDuration) {
        this.chronoUnit = chronoUnit;
        this.duration = estimatedDuration;
    }
}