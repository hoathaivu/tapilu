package htv.springboot.apps.webscraper.job.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
@Table(value = "jobs")
public class Job {
    @PrimaryKey
    private String jobId;
    private String companyId;
    private OffsetDateTime postedDatetime;
    private String jobTitle;
    private String jobUrl;
    private String source;
    @Transient
    private JobDetail jobDetail;

    @Override
    public String toString() {
        return String.format(
                "source: %s\nid: %s\ncompany: %s\nposted date: %s\ntitle: %s\nurl: %s",
                getSource(),
                getJobId(),
                getCompanyId(),
                getPostedDatetime().toLocalDateTime(),
                getJobTitle(),
                getJobUrl())
                + "\n"
                + getJobDetail().toShortString();
    }
}
