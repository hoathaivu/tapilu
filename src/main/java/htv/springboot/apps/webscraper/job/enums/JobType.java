package htv.springboot.apps.webscraper.job.enums;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public enum JobType {
    FULL_TIME("fulltime", "Full Time"),
    PART_TIME("parttime", "Part Time"),
    CONTRACT("contract", "Contract"),
    UNKNOWN("unknown", "Unknown");

    private final String name;
    private final String displayName;

    private static final Map<String, JobType> ENUM_MAP;

    static {
        Map<String, JobType> tempMap = new ConcurrentHashMap<>();
        for (JobType jobType : JobType.values()) {
            tempMap.put(jobType.name, jobType);
        }

        ENUM_MAP = Collections.unmodifiableMap(tempMap);
    }

    JobType(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public static JobType getEnum(String jobTypeVal) {
        String assumedVal = jobTypeVal
                .toLowerCase()
                .replaceAll("[-_ ]", "");
        if (ENUM_MAP.containsKey(assumedVal)) {
            return ENUM_MAP.get(assumedVal);
        }

        return JobType.UNKNOWN;
    }
}
