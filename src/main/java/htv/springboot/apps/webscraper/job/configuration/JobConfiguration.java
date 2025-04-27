package htv.springboot.apps.webscraper.job.configuration;

import htv.springboot.apps.webscraper.job.JobScraper;
import htv.springboot.apps.webscraper.job.JobService;
import htv.springboot.apps.webscraper.job.beans.Job;
import htv.springboot.utils.MailUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Configuration
public class JobConfiguration {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private List<JobScraper> jobScrapers;

    @Autowired
    private ThreadPoolTaskExecutor virtualTaskExecutor;

    @Autowired
    private JobService jobService;

    @Scheduled(fixedDelayString = "${htv.springboot.job.schedule-fixed-delay}", timeUnit = TimeUnit.HOURS)
    public void scheduleScraping() {
        LOGGER.info("Start initiating job-scraping threads");

        for (JobScraper jobScraper : jobScrapers) {
            virtualTaskExecutor.submitCompletable(() -> {
                List<Job> scrapedJobs = jobScraper.retrieveJobs();
                LOGGER.info("Scraped {} jobs from {}", scrapedJobs.size(), jobScraper.getJobSiteName());
                return scrapedJobs;
            }).whenComplete((scrapedJobs, e) -> {
                if (e != null) {
                    LOGGER.error("Failed to scrape job from {}", jobScraper.getJobSiteName(), e);
                } else {
                    jobService.updateAndProcessJobQueue(scrapedJobs);
                }
            });
        }

        LOGGER.info("Finished initiating job-scraping threads");
    }

    private static final String EMAIL_CHANNEL = "receiveEmailChannel";
    private static final String JAVAMAIL_IMAP_PROPERTIES_PREFIX = "mail.imap";

    @Value("${htv.springboot.job.email-address}")
    private String emailAddress;

    @Value("${htv.springboot.job.email-app-pass}")
    private String emailAppPassword;

    @ConfigurationProperties(prefix = JAVAMAIL_IMAP_PROPERTIES_PREFIX)
    @Bean
    public Properties imapProperties() {
        return new Properties();
    }

    @Bean
    public IntegrationFlow imapIdleMailFlow(Properties imapProperties) {
        imapProperties.forEach((k, _) -> imapProperties.setProperty(
                JAVAMAIL_IMAP_PROPERTIES_PREFIX + "." + k,
                (String) imapProperties.remove(k)));

        return IntegrationFlow
                .from(Mail.imapIdleAdapter(MailUtils.getImapUrl(emailAddress, emailAppPassword))
                        .autoStartup(true)
                        .javaMailProperties(imapProperties)
                        .shouldDeleteMessages(false)
                        .shouldMarkMessagesAsRead(true)
                        .shouldReconnectAutomatically(true)
                        .simpleContent(true))
                .channel(EMAIL_CHANNEL)
                .get();
    }

    @ServiceActivator(inputChannel = EMAIL_CHANNEL)
    public void handleMessage(Message<MimeMessage> message) {
        try {
            jobService.processNewEmail(message);
        } catch (MessagingException | IOException e) {
            LOGGER.error("Error processing new email", e);
        }
    }
}
