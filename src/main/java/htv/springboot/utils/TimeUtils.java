package htv.springboot.utils;

import htv.springboot.enums.ChronoWorkUnit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static htv.springboot.enums.ChronoWorkUnit.DAYS;
import static htv.springboot.enums.ChronoWorkUnit.HOURS;
import static htv.springboot.enums.ChronoWorkUnit.MINUTES;
import static htv.springboot.enums.ChronoWorkUnit.MONTHS;
import static htv.springboot.enums.ChronoWorkUnit.SECONDS;
import static htv.springboot.enums.ChronoWorkUnit.WEEKS;
import static htv.springboot.enums.ChronoWorkUnit.YEARS;

public class TimeUtils {
    public static final Set<String> SECOND_SHORTHANDS = Set.of("s", "sec", "second", "seconds");
    public static final Set<String> MINUTE_SHORTHANDS = Set.of("m", "minute", "minutes");
    public static final Set<String> HOUR_SHORTHANDS = Set.of("h", "hr", "hour", "hours");
    public static final Set<String> DAY_SHORTHANDS = Set.of("d", "day", "days");
    public static final Set<String> WEEK_SHORTHANDS = Set.of("w", "wk", "week", "weeks");
    public static final Set<String> MONTH_SHORTHANDS = Set.of("mth", "month", "months");
    public static final Set<String> YEAR_SHORTHANDS = Set.of("y", "yr", "year", "years");
    public static final Map<String, ChronoWorkUnit> SHORTHAND_CHRONO_UNIT_MAP;

    static {
        Map<String, ChronoWorkUnit> tempMap = new HashMap<>();
        SECOND_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, SECONDS));
        MINUTE_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, MINUTES));
        HOUR_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, HOURS));
        DAY_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, DAYS));
        WEEK_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, WEEKS));
        MONTH_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, MONTHS));
        YEAR_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, YEARS));
        SHORTHAND_CHRONO_UNIT_MAP = Collections.unmodifiableMap(tempMap);
    }

    public static double convert(double fromValue, String fromUnitShorthand, ChronoWorkUnit toUnit) {
        if (!SHORTHAND_CHRONO_UNIT_MAP.containsKey(fromUnitShorthand)) {
            throw new UnsupportedOperationException();
        }

        return convert(fromValue, SHORTHAND_CHRONO_UNIT_MAP.get(fromUnitShorthand), toUnit);
    }

    public static double convert(double fromValue, ChronoWorkUnit fromUnit, ChronoWorkUnit toUnit) {
        return fromValue * toUnit.getDuration().toSeconds() / fromUnit.getDuration().toSeconds();
    }
}
