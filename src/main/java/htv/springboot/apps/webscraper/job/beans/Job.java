package htv.springboot.apps.webscraper.job.beans;

import com.google.gson.JsonObject;
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
    @Transient
    private JobDetail jobDetail;

    public Job(JsonObject jsonObject) {
        jobId = jsonObject.get("id").getAsString();
        jobUrl = jsonObject.get("apply_url").getAsString();

        JsonObject processedJob = jsonObject.getAsJsonObject("v5_processed_job_data");
        companyId = processedJob.has("company_name") && !processedJob.get("company_name").isJsonNull() ?
                processedJob.get("company_name").getAsString()
                : processedJob.get("company_website").getAsString();
        postedDatetime = OffsetDateTime.parse(processedJob.get("estimated_publish_date").getAsString());

        JsonObject jobInformation = jsonObject.getAsJsonObject("job_information");
        jobTitle = jobInformation.get("title").getAsString();

        jobDetail = new JobDetail(jobId, jsonObject);
    }
}
