package htv.springboot.apps.webscraper.job;

import htv.springboot.utils.MailUtils;
import htv.springboot.utils.StringUtils;
import jakarta.mail.Flags;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static htv.springboot.utils.MailUtils.UNSUBSCRIBE_HEADER;
import static java.time.OffsetDateTime.now;
import static javax.swing.JOptionPane.CLOSED_OPTION;

@Service
public class JobService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private Browser browser;

    @Autowired
    private CostOfLivingService coLService;

    @Autowired
    private JobDatabaseService databaseService;

    private static final int SKIP_OPTION = 0;
    private static final int PROCEED_OPTION = 1;
    private static final int PAUSE_1_DAY_OPTION = 2;
    private static final String[] BUTTON_OPTIONS = new String[] {"Skip", "Proceed", "Pause for 1 day"};

    private static OffsetDateTime scrapeProcessPauseStartTime = null;

    private final Queue<Job> jobQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean scrapeProcessingStart = new AtomicBoolean(false);

    public void updateAndProcessJobQueue(List<Job> scrapedJobs) {
        jobQueue.addAll(scrapedJobs);

        if (isInPausePeriod()) {
            return;
        }

        if (scrapeProcessingStart.compareAndSet(false, true)) {
            LOGGER.info("Scrape process has not been started - starting");
            Job job;
            while ((job = jobQueue.poll()) != null) {
                try {
                    LOGGER.trace("Processing job {}", job.getJobId());
                    if (!databaseService.isSeenJob(job)) {
                        LOGGER.trace("New job");
                        processNewJob(job);

                        if (isInPausePeriod()) {
                            break;
                        }
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
            scrapeProcessingStart.set(false);

            LOGGER.info("Finish processing scraped job(s)");
        } else {
            LOGGER.info("Scrape process has been started already");
        }
    }

    private boolean isInPausePeriod() {
        if (scrapeProcessPauseStartTime != null) {
            if (now().minusDays(1).isBefore(scrapeProcessPauseStartTime)) {
                LOGGER.info("Process is in pause period");
                return true;
            } else {
                LOGGER.info("Pause period is over");
                scrapeProcessPauseStartTime = null;
            }
        }

        return false;
    }

    private void processNewJob(Job job) {
        int selected = JavaUtils.createOptionWindow("New Job", StringUtils.escape(job.toString()), BUTTON_OPTIONS);
        LOGGER.trace("{} option clicked", selected == CLOSED_OPTION ? "Close" : BUTTON_OPTIONS[selected]);
        switch (selected) {
            case CLOSED_OPTION:
            case SKIP_OPTION:
                createInitialRecords(job);
                databaseService.updateJobStatsStatus(job.getJobId(), JobStatsStatus.SKIPPED);
                break;
            case PROCEED_OPTION:
                createInitialRecords(job);
                browser.openUrl(job.getJobUrl());
                databaseService.createNewAppliedJob(job);
                databaseService.updateJobStatsStatus(job.getJobId(), JobStatsStatus.APPLIED);
                break;
            case PAUSE_1_DAY_OPTION:
                scrapeProcessPauseStartTime = now();
                break;
            default:
                LOGGER.error("Unknown option: {}", selected);
        }
    }

    private void createInitialRecords(Job job) {
        //create new record in jobs
        databaseService.createNewJob(job);

        //create new record in jobs_details
        databaseService.createNewJobDetail(job.getJobDetail());

        //create new record in jobs_statistics
        Map<String, CoLExcelBean> coLMap = coLService.getCoL(job.getJobDetail().getJobLocation().split("\\|"));
        double maxCoL = 0;
        for (CoLExcelBean bean : coLMap.values()) {
            maxCoL = Math.max(maxCoL, bean.getYearlyCost().getTotal());
        }

        databaseService.createNewJobStatistics(new JobStats(job, maxCoL));
    }

    private static final int EMAIL_SKIP_OPTION = 0;
    private static final int EMAIL_DELETE_OPTION = 1;
    private static final int EMAIL_UNSUBSCRIBE_DELETE_OPTION = 2;
    private static final String[] NORM_EMAIL_BUTTON_OPTIONS = new String[] {"Skip", "Delete"};
    private static final String[] AD_EMAIL_BUTTON_OPTIONS = new String[] {"Skip", "Delete", "Unsubscribe and Delete"};

    @Value("${htv.springboot.job.email-address}")
    private String emailAddress;

    @Value("${htv.springboot.job.email-app-pass}")
    private String emailAppPassword;

    @Autowired
    private RestClient restClient;

    public void processNewEmail(Message<MimeMessage> mimeMsg) throws MessagingException, IOException {
        LOGGER.info("Start processNewEmail");
        /**
         * 1. When receive job email, check email's content for info
         * 	a. If new jobAppliedStatus, update jobs_applied. If not rejected, go to step #2
         * 	b. If no news, ignore
         * 2. Send notification of the news, together with job's full information
         */
        MimeMessage email = mimeMsg.getPayload();

        String msg = String.format("From: %s\nTo: %s\nSubject: %s",
                StringUtils.toString(email.getFrom()),
                StringUtils.toString(email.getAllRecipients()),
                email.getSubject());

        msg = StringUtils.escape(msg) + "\nContent:\n" + MailUtils.getEmailContent(email);

        String[] buttons = email.getHeader(UNSUBSCRIBE_HEADER) != null ?
                AD_EMAIL_BUTTON_OPTIONS : NORM_EMAIL_BUTTON_OPTIONS;

        try {
            boolean deletionInitiated = false;
            switch (JavaUtils.createOptionWindow("New email", msg, buttons)) {
                case EMAIL_DELETE_OPTION:
                    email.setFlag(Flags.Flag.USER, true);
                    deletionInitiated = true;
                    break;
                case EMAIL_UNSUBSCRIBE_DELETE_OPTION:
                    MailUtils.unsubscribeFromEmail(email, restClient);
                    email.setFlag(Flags.Flag.USER, true);
                    deletionInitiated = true;
                    break;
                case EMAIL_SKIP_OPTION:
                default:
            }

            if (deletionInitiated) {
                MailUtils.deleteFlaggedEmails(emailAddress, emailAppPassword, Flags.Flag.USER);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Message too long: {}", msg.length(), e);
        } catch (Exception e) {
            LOGGER.error("Error displaying msg: {}", msg, e);
        }

        LOGGER.info("Finished processNewEmail");
    }
}
