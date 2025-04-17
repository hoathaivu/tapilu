package htv.springboot.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TimeConstant {
    public static final Set<String> HOUR_SHORTHANDS = Set.of("h", "hr", "hour");
    public static final Set<String> DAY_SHORTHANDS = Set.of("d", "day");
    public static final Set<String> WEEK_SHORTHANDS = Set.of("w", "wk", "week");
    public static final Set<String> MONTH_SHORTHANDS = Set.of("m", "month");
    public static final Set<String> YEAR_SHORTHANDS = Set.of("y", "yr", "year");
    public static final Map<String, Double> SHORTHAND_MULT_MAP;

    static {
        Map<String, Double> tempMap = new HashMap<>();
        HOUR_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, 8760D));
        DAY_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, 365D));
        WEEK_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, 52.143));
        MONTH_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, 12D));
        YEAR_SHORTHANDS.forEach(shorthand -> tempMap.put(shorthand, 1D));
        SHORTHAND_MULT_MAP = Collections.unmodifiableMap(tempMap);
    }
}
