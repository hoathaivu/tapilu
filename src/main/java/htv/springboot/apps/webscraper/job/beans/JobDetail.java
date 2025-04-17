package htv.springboot.apps.webscraper.job.beans;

import htv.springboot.apps.webscraper.job.enums.JobType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Setter
@Getter
@NoArgsConstructor
@Table(value = "jobs_details")
public class JobDetail {
    @PrimaryKey
    private String jobId;
    private String jobDescription;
    private JobType jobType;
    private String jobLocation;
    private String jobLocationType;
    private Double compensationMin;
    private Double compensationMax;

    public String toShortString() {
        return String.format(
                "type: %s\nlocation: %s\nwork location: %s\ncomp min: %s\ncomp max: %s",
                getJobType().getDisplayName(),
                getJobLocation(),
                getJobLocationType(),
                getCompensationMin() == null ? "" : getCompensationMin(),
                getCompensationMax() == null ? "" : getCompensationMax());
    }
}
