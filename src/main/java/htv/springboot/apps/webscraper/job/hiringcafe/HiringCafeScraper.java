package htv.springboot.apps.webscraper.job.hiringcafe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import htv.springboot.apps.webscraper.job.beans.JobDetail;
import htv.springboot.apps.webscraper.job.enums.JobType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import htv.springboot.utils.FileUtils;
import htv.springboot.apps.webscraper.BaseScrapper;
import htv.springboot.apps.webscraper.job.JobScraper;
import htv.springboot.apps.webscraper.job.beans.Job;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HiringCafeScraper extends BaseScrapper implements JobScraper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String REQUEST_URL = "https://hiring.cafe/api/search-jobs";
    private static final String REQUEST_BODY_FILEPATH = "webscraper/job/hiringcafe/searchjobs.txt";

    public List<Job> retrieveJobs() throws URISyntaxException, IOException {
        JsonArray result = JsonParser
                .parseString(retrieveResponse(REQUEST_URL, HttpMethod.POST, getRequestBody()))
                .getAsJsonObject()
                .get("results")
                .getAsJsonArray();

        List<Job> jobList = new ArrayList<>();
        for (JsonElement jsonElement : result.asList()) {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Job job = new Job();
                job.setJobId(jsonObject.get("id").getAsString());
                job.setJobUrl(jsonObject.get("apply_url").getAsString());
                job.setSource(getJobSiteName());

                JsonObject processedJob = jsonObject.getAsJsonObject("v5_processed_job_data");
                job.setCompanyId(processedJob.has("company_name") && !processedJob.get("company_name").isJsonNull() ?
                        processedJob.get("company_name").getAsString()
                        : processedJob.get("company_website").getAsString());
                job.setPostedDatetime(OffsetDateTime.parse(processedJob.get("estimated_publish_date").getAsString()));

                JsonObject jobInformation = jsonObject.getAsJsonObject("job_information");
                job.setJobTitle(jobInformation.get("title").getAsString());

                parseJobDetails(job, jsonObject, jobInformation);

                jobList.add(job);
            } catch (Exception e) {
                LOGGER.error("Failed to parse JsonElement:\n{}", jsonElement, e);
            }
        }
        return jobList;
    }

    private String getRequestBody() throws URISyntaxException, IOException {
        return FileUtils.getResourceContent(REQUEST_BODY_FILEPATH);
    }

    private void parseJobDetails(Job job, JsonObject jsonObject, JsonObject jobInformation) {
        JobDetail jobDetail = new JobDetail();
        jobDetail.setJobId(job.getJobId());

        JsonObject processedJob = jsonObject.getAsJsonObject("v5_processed_job_data");
        jobDetail.setJobType(JobType.getEnum(processedJob.get("commitment").getAsJsonArray()
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.joining(","))));
        jobDetail.setJobLocation(processedJob.get("workplace_cities").getAsJsonArray()
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.joining("|")));
        jobDetail.setJobLocationType(processedJob.get("workplace_type").getAsString());
        jobDetail.setCompensationMin(processedJob.get("yearly_min_compensation").isJsonNull() ?
                null : processedJob.get("yearly_min_compensation").getAsDouble());
        jobDetail.setCompensationMax(processedJob.get("yearly_max_compensation").isJsonNull() ?
                null : processedJob.get("yearly_max_compensation").getAsDouble());
        jobDetail.setJobDescription(jobInformation.get("description").getAsString());

        job.setJobDetail(jobDetail);
    }

    @Override
    public String getJobSiteName() {
        return "HiringCafe";
    }
}
