package htv.springboot.apps.webscraper.job.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import htv.springboot.apps.webscraper.job.enums.JobStatsStatus;
import htv.springboot.apps.webscraper.job.beans.AppliedJob;
import htv.springboot.apps.webscraper.job.beans.Job;
import htv.springboot.apps.webscraper.job.beans.JobDetail;
import htv.springboot.apps.webscraper.job.beans.JobStats;
import htv.springboot.apps.webscraper.job.db.repositories.JobsAppliedRepository;
import htv.springboot.apps.webscraper.job.db.repositories.JobsDetailsRepository;
import htv.springboot.apps.webscraper.job.db.repositories.JobsRepository;
import htv.springboot.apps.webscraper.job.db.repositories.JobsStatisticsRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

@Service
public class JobDatabaseService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private JobsRepository jobsRepository;

    @Autowired
    private JobsDetailsRepository jobsDetailsRepository;

    @Autowired
    private JobsAppliedRepository jobsAppliedRepository;

    @Autowired
    private JobsStatisticsRepository jobsStatisticsRepository;

    public boolean isSeenJob(Job job) {
        return jobsRepository.existsById(job.getJobId());
    }

    public boolean isBeforeDays(Job job, int dayCount) {
        return jobsRepository.getLastUpdateDatetime(job.getJobId())
                .map(instant -> instant.isBefore(Instant.now().minus(dayCount, ChronoUnit.DAYS)))
                .orElse(false);
    }

    public void createNewJob(Job job) {
        LOGGER.trace("Creating new jobs record");
        jobsRepository.save(job);
    }

    public void createNewJobDetail(JobDetail jobDetail) {
        LOGGER.trace("Creating new jobs_details record");
        jobsDetailsRepository.save(jobDetail);
    }

    public void createNewAppliedJob(Job job) {
        LOGGER.trace("Creating new jobs_applied record");
        jobsAppliedRepository.save(new AppliedJob(job));
    }

    public void createNewJobStatistics(JobStats jobStats) {
        LOGGER.trace("Creating new jobs_statistics record");
        jobsStatisticsRepository.save(jobStats);
    }

    public void updateJobStatsStatus(String jobId, JobStatsStatus status) {
        LOGGER.trace("Updating jobs_statistics record's {} for id {}", status, jobId);
        jobsStatisticsRepository.updateStatus(status, jobId);
    }

    public void updateJobStatsEncounter(Job job) {
        LOGGER.trace("Updating jobs_statistics record's encounter for id {}", job.getJobId());
        Optional<JobStats> jobStatsOpt = jobsStatisticsRepository.findById(job.getJobId());

        if (jobStatsOpt.isPresent()) {
            JobStats jobStats = jobStatsOpt.get();
            Set<OffsetDateTime> set = new HashSet<>(jobStats.getSeenOn());
            set.add(job.getPostedDatetime());
            jobStats.setSeenOn(set);
            jobsStatisticsRepository.save(jobStats);
        } else {
            LOGGER.trace("Record in jobs_statistics not found with id {}", job.getJobId());
        }
    }
}
