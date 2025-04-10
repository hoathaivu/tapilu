package htv.springboot.apps.webscraper.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import htv.springboot.apps.webscraper.job.db.JobDatabaseService;
import htv.springboot.browsers.Browser;
import htv.springboot.apps.webscraper.job.enums.JobStatsStatus;
import htv.springboot.utils.JavaUtils;
import htv.springboot.apps.webscraper.job.beans.Job;
import htv.springboot.apps.webscraper.job.beans.JobStats;
import htv.springboot.apps.webscraper.job.costofliving.CostOfLivingService;
import htv.springboot.apps.webscraper.job.costofliving.beans.CoLExcelBean;

import javax.swing.JOptionPane;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JobService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SKIP_OPTION = 0;
    private static final int PROCEED_OPTION = 1;
    private static final String[] BUTTON_OPTIONS = new String[] {"Skip", "Proceed"};

    @Autowired
    private JobScraper jobScraper;

    @Autowired
    private Browser browser;

    @Autowired
    private JobDatabaseService databaseService;

    @Autowired
    private CostOfLivingService coLService;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void scheduleScraping() throws Exception {
        LOGGER.info("Start scheduleScraping");
        for (Job job : jobScraper.retrieveJobs()) {
            try {
                LOGGER.trace("Processing job {}", job.getJobId());
                if (!databaseService.isSeenJob(job)) {
                    LOGGER.trace("New job");
                    processNewJob(job);
                } else if (databaseService.isBeforeDays(job, 1)) {
                    LOGGER.trace("Job reappeared");
                    databaseService.updateJobStatsEncounter(job);
                } else {
                    LOGGER.trace("Job already processed");
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
        LOGGER.info("Finished scheduleScraping");
    }

    private void processNewJob(Job job) {
        //create new record in jobs
        databaseService.createNewJob(job);

        //create new record in jobs_details
        databaseService.createNewJobDetail(job.getJobDetail());

        //create new record in jobs_statistics
        createNewJobStatistics(job);

        String msg = String.format(
                "company: %s\npostedDate: %s\ntitle: %s\nurl: %s\ntype: %s\nlocation: %s\nwork location: %s\ncomp min: %s\ncomp max: %s",
                job.getCompanyId(),
                job.getPostedDatetime().toLocalDateTime(),
                job.getJobTitle(),
                job.getJobUrl(),
                job.getJobDetail().getJobType(),
                job.getJobDetail().getJobLocation(),
                job.getJobDetail().getJobLocationType(),
                job.getJobDetail().getCompensationMin() == null ? "" : job.getJobDetail().getCompensationMin(),
                job.getJobDetail().getCompensationMax() == null ? "" : job.getJobDetail().getCompensationMax());

        int selected = JavaUtils.createOptionWindow("New Job", msg, BUTTON_OPTIONS, 1);
        switch (selected) {
            case JOptionPane.CLOSED_OPTION:
                LOGGER.trace("Closed option clicked");
            case SKIP_OPTION:
                LOGGER.trace("Skip option clicked");
                databaseService.updateJobStatsStatus(job.getJobId(), JobStatsStatus.SKIPPED);
                break;
            case PROCEED_OPTION:
                LOGGER.trace("Proceed option clicked");
                browser.openUrl(job.getJobUrl());
                databaseService.createNewAppliedJob(job);
                databaseService.updateJobStatsStatus(job.getJobId(), JobStatsStatus.APPLIED);
                break;
            default:
                LOGGER.error("Unknown option: {}", selected);
        }
    }

    private void createNewJobStatistics(Job job) {
        Map<String, CoLExcelBean> coLMap = coLService.getCoL(job.getJobDetail().getJobLocation().split("\\|"));
        double maxCoL = 0;
        for (CoLExcelBean bean : coLMap.values()) {
            maxCoL = Math.max(maxCoL, bean.getYearlyCost().getTotal());
        }

        databaseService.createNewJobStatistics(new JobStats(job, maxCoL));
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void scheduleEmailCheck() {
        /**
         * 1. When receive job email, check email's content for info
         * 	a. If new jobAppliedStatus, update jobs_applied. If not rejected, go to step #2
         * 	b. If no news, ignore
         * 2. Send notification of the news, together with job's full information
         */
    }
}
