package htv.springboot.apps.webscraper.job.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import htv.springboot.apps.webscraper.job.enums.AppliedJobStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Table(value = "jobs_applied")
public class AppliedJob {
    @PrimaryKey
    private String jobId;
    private OffsetDateTime appliedDatetime;
    private AppliedJobStatus status;

    public AppliedJob(Job job) {
        jobId = job.getJobId();
        appliedDatetime = job.getPostedDatetime();
        status = AppliedJobStatus.APPLIED;
    }
}
