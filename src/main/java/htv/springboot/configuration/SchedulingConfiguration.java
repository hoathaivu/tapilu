package htv.springboot.configuration;

import htv.springboot.utils.MailUtils;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Configuration
@EnableScheduling
public class SchedulingConfiguration {

    private static final Logger LOGGER = LogManager.getLogger();

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public CommandLineRunner schedulingRunner(
            @Value("${htv.springboot.email.email-address-env-name}") String emailAddressEnvName,
            @Value("${htv.springboot.email.email-app-pass-env-name}") String emailAppPasswordEnvName,
            Environment environment,
            TaskExecutor taskExecutor, RestClient restClient) {
        return _ -> taskExecutor.execute(() -> {
            try {
                MailUtils.setupIdle(
                        environment.getProperty(emailAddressEnvName),
                        environment.getProperty(emailAppPasswordEnvName),
                        restClient);
            } catch (MessagingException | IOException e) {
                LOGGER.error("Error setting up IDLE for emailing", e);
            }
        });
    }
}
