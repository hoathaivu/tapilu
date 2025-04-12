package htv.springboot.apps.webscraper.job;

import htv.springboot.utils.MailUtils;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
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
import org.springframework.web.client.RestClient;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static htv.springboot.utils.MailUtils.UNSUBSCRIBE_HEADER;

@Service
public class JobService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SKIP_OPTION = 0;
    private static final int PROCEED_OPTION = 1;
    private static final String[] BUTTON_OPTIONS = new String[] {"Skip", "Proceed"};

    @Autowired
    private Browser browser;

    @Autowired
    private CostOfLivingService coLService;

    @Autowired
    private JobDatabaseService databaseService;

    @Autowired
    private JobScraper jobScraper;

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
                LOGGER.error(e.getMessage(), e);
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

    public static final String YAHOOMAIL_EMAIL_ENV_NAME = "YAHOOMAIL_EMAIL";
    public static final String YAHOOMAIL_PASSWORD_ENV_NAME = "YAHOOMAIL_APPLICATION_PASSWORD";

    private static final int EMAIL_SKIP_OPTION = 0;
    private static final int EMAIL_DELETE_OPTION = 1;
    private static final int EMAIL_UNSUBSCRIBE_DELETE_OPTION = 2;
    private static final String[] NORM_EMAIL_BUTTON_OPTIONS = new String[] {"Skip", "Delete"};
    private static final String[] AD_EMAIL_BUTTON_OPTIONS = new String[] {"Skip", "Delete", "Unsubscribe and Delete"};

    public static void processNewEmails(Message[] newEmails, RestClient restClient)
            throws MessagingException, IOException {
        LOGGER.info("Start processNewEmails");
        /**
         * 1. When receive job email, check email's content for info
         * 	a. If new jobAppliedStatus, update jobs_applied. If not rejected, go to step #2
         * 	b. If no news, ignore
         * 2. Send notification of the news, together with job's full information
         */
        for (Message email : newEmails) {
            String msg = String.format("From: %s\nTo: %s\nSubject: %s\nContent:\n%s",
                    Arrays.stream(email.getFrom()).map(Address::toString).collect(Collectors.joining(",")),
                    Arrays.stream(email.getAllRecipients()).map(Address::toString).collect(Collectors.joining(",")),
                    email.getSubject(),
                    MailUtils.getPlainEmailContent(email));

            String[] buttons = NORM_EMAIL_BUTTON_OPTIONS;
            Enumeration<Header> enumeration = email.getAllHeaders();
            while (enumeration.hasMoreElements()) {
                Header header = enumeration.nextElement();
                if (UNSUBSCRIBE_HEADER.equals(header.getName())) {
                    buttons = AD_EMAIL_BUTTON_OPTIONS;
                    break;
                }
            }

            int selected = JavaUtils.createOptionWindow("New email", msg, buttons, 0);
            switch (selected) {
                case EMAIL_DELETE_OPTION:
                    email.setFlag(Flags.Flag.USER, true);
                    break;
                case EMAIL_UNSUBSCRIBE_DELETE_OPTION:
                    email.setFlag(Flags.Flag.USER, true);
                    MailUtils.unsubscribeFromEmail(email, restClient);
                    break;
                case EMAIL_SKIP_OPTION:
                default:
            }
        }

        String emailAddress = System.getenv(YAHOOMAIL_EMAIL_ENV_NAME);
        String emailAppPassword = System.getenv(YAHOOMAIL_PASSWORD_ENV_NAME);
        MailUtils.deleteFlaggedEmails(emailAddress, emailAppPassword, Flags.Flag.USER);

        LOGGER.info("Finished processNewEmails");
    }
}
