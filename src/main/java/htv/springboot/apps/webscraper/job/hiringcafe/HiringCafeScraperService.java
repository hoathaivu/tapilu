package htv.springboot.apps.webscraper.job.hiringcafe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import htv.springboot.utils.FileUtils;
import htv.springboot.apps.webscraper.ScrapperService;
import htv.springboot.apps.webscraper.job.JobScraper;
import htv.springboot.apps.webscraper.job.beans.Job;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HiringCafeScraperService extends ScrapperService implements JobScraper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String REQUEST_URL = "https://hiring.cafe/api/search-jobs";
    private static final String REQUEST_BODY_FILEPATH = "webscraper/hiringcafe/searchjobs.txt";

    public List<Job> retrieveJobs() throws URISyntaxException, IOException {
        JsonArray result = JsonParser
                .parseString(retrieveResponse(REQUEST_URL, HttpMethod.POST, getRequestBody()))
                .getAsJsonObject()
                .get("results")
                .getAsJsonArray();

        List<Job> jobList = new ArrayList<>();
        for (JsonElement jsonElement : result.asList()) {
            try {
                jobList.add(new Job(jsonElement.getAsJsonObject()));
            } catch (Exception e) {
                LOGGER.error("Failed to parse JsonElement:\n{}", jsonElement, e);
            }
        }
        return jobList;
    }

    private String getRequestBody() throws URISyntaxException, IOException {
        return FileUtils.getResourceContent(REQUEST_BODY_FILEPATH);
    }
}
