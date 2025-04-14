package htv.springboot.apps.webscraper.job.beans;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@Table(value = "jobs_details")
public class JobDetail {
    @PrimaryKey
    private String jobId;
    private String jobDescription;
    private String jobType;
    private String jobLocation;
    private String jobLocationType;
    private Double compensationMin;
    private Double compensationMax;

    public JobDetail(String jobId, JsonObject jsonObject) {
        this.jobId = jobId;

        JsonObject processedJob = jsonObject.getAsJsonObject("v5_processed_job_data");
        jobType = processedJob.get("commitment").getAsJsonArray()
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.joining(","));
        jobLocation = processedJob.get("workplace_cities").getAsJsonArray()
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.joining("|"));
        jobLocationType = processedJob.get("workplace_type").getAsString();
        compensationMin = processedJob.get("yearly_min_compensation").isJsonNull() ?
                null : processedJob.get("yearly_min_compensation").getAsDouble();
        compensationMax = processedJob.get("yearly_max_compensation").isJsonNull() ?
                null : processedJob.get("yearly_max_compensation").getAsDouble();

        JsonObject jobInformation = jsonObject.getAsJsonObject("job_information");
        jobDescription = jobInformation.get("description").getAsString();
    }

    public String toShortString() {
        return String.format(
                "type: %s\nlocation: %s\nwork location: %s\ncomp min: %s\ncomp max: %s",
                getJobType(),
                getJobLocation(),
                getJobLocationType(),
                getCompensationMin() == null ? "" : getCompensationMin(),
                getCompensationMax() == null ? "" : getCompensationMax());
    }
}
