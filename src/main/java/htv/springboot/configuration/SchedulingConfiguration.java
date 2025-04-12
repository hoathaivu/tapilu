package htv.springboot.configuration;

import htv.springboot.utils.MailUtils;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static htv.springboot.apps.webscraper.job.JobService.YAHOOMAIL_EMAIL_ENV_NAME;
import static htv.springboot.apps.webscraper.job.JobService.YAHOOMAIL_PASSWORD_ENV_NAME;

@Configuration
@EnableScheduling
public class SchedulingConfiguration {

    private static final Logger LOGGER = LogManager.getLogger();

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public CommandLineRunner schedulingRunner(TaskExecutor taskExecutor, RestClient restClient) {
        return _ -> taskExecutor.execute(() -> {
            try {
                String emailAddress = System.getenv(YAHOOMAIL_EMAIL_ENV_NAME);
                String emailAppPassword = System.getenv(YAHOOMAIL_PASSWORD_ENV_NAME);
                MailUtils.setupIdle(emailAddress, emailAppPassword, restClient);
            } catch (MessagingException | IOException e) {
                LOGGER.error("Error setting up IDLE for emailing", e);
            }
        });
    }
}
