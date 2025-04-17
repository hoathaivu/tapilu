package htv.springboot.apps.webscraper.job;

import htv.springboot.apps.webscraper.job.beans.Job;

import java.util.List;

public interface JobScraper {
    List<Job> retrieveJobs() throws Exception;
    String getJobSiteName();
}
