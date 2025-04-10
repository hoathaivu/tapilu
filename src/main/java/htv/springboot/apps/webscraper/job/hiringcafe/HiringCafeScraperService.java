package htv.springboot.apps.webscraper.job.hiringcafe;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
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
import java.util.stream.Collectors;

@Service
public class HiringCafeScraperService extends ScrapperService implements JobScraper {

    private static final String REQUEST_URL = "https://hiring.cafe/api/search-jobs";
    private static final String REQUEST_BODY_FILEPATH = "webscraper/hiringcafe/searchjobs.txt";

    public List<Job> retrieveJobs() throws URISyntaxException, IOException {
        JsonArray result = JsonParser
                .parseString(retrieveResponse(REQUEST_URL, HttpMethod.POST, getRequestBody()))
                .getAsJsonObject()
                .get("results")
                .getAsJsonArray();

        return result
                .asList()
                .stream()
                .map(jsonElement -> new Job(jsonElement.getAsJsonObject()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String getRequestBody() throws URISyntaxException, IOException {
        return FileUtils.getResourceContent(REQUEST_BODY_FILEPATH);
    }
}
