package htv.springboot.apps.webscraper.job.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import htv.springboot.apps.webscraper.job.enums.JobStatsStatus;

import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Table(value = "jobs_statistics")
public class JobStats {
    @PrimaryKey
    private String jobId;
    private JobStatsStatus jobAppliedStatus;
    private Set<OffsetDateTime> seenOn;
    private double jobLocationCol;

    public JobStats(Job job, double coL) {
        jobId = job.getJobId();
        seenOn = Set.of(job.getPostedDatetime());
        jobAppliedStatus = JobStatsStatus.NEW;
        jobLocationCol = coL;
    }
}
