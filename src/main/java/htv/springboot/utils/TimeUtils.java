package htv.springboot.utils;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

public class TimeUtils {
    public static final Set<String> SECOND_SHORTHANDS = Set.of("s", "sec", "second", "seconds");
    public static final Set<String> MINUTE_SHORTHANDS = Set.of("m", "minute", "minutes");
    public static final Set<String> HOUR_SHORTHANDS = Set.of("h", "hr", "hour", "hours");
    public static final Set<String> DAY_SHORTHANDS = Set.of("d", "day", "days");
    public static final Set<String> WEEK_SHORTHANDS = Set.of("w", "wk", "week", "weeks");
    public static final Set<String> MONTH_SHORTHANDS = Set.of("mth", "month", "months");
    public static final Set<String> YEAR_SHORTHANDS = Set.of("y", "yr", "year", "years");
    public static final Map<String, ChronoUnit> SHORTHAND_CHRONO_UNIT_MAP;

    static {
        Map<String, ChronoUnit> tempMap = new HashMap<>();
        SECOND_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, SECONDS));
        MINUTE_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, MINUTES));
        HOUR_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, HOURS));
        DAY_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, DAYS));
        WEEK_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, WEEKS));
        MONTH_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, MONTHS));
        YEAR_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, YEARS));
        SHORTHAND_CHRONO_UNIT_MAP = Collections.unmodifiableMap(tempMap);
    }

    public static double convert(double fromValue, String fromUnitShorthand, ChronoUnit toUnit) {
        if (!SHORTHAND_CHRONO_UNIT_MAP.containsKey(fromUnitShorthand)) {
            throw new UnsupportedOperationException();
        }

        return convert(fromValue, SHORTHAND_CHRONO_UNIT_MAP.get(fromUnitShorthand), toUnit);
    }

    public static double convert(double fromValue, ChronoUnit fromUnit, ChronoUnit toUnit) {
        return fromValue * ((double) fromUnit.getDuration().toSeconds() / toUnit.getDuration().toSeconds());
    }
}
