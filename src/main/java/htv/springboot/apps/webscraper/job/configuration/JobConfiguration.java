package htv.springboot.apps.webscraper.job.configuration;

import htv.springboot.apps.webscraper.job.JobService;
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

import java.io.IOException;
import java.util.Properties;

@Configuration
public class JobConfiguration {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String EMAIL_CHANNEL = "receiveEmailChannel";
    private static final String JAVAMAIL_IMAP_PROPERTIES_PREFIX = "mail.imap";

    @Value("${htv.springboot.job.email-address}")
    private String emailAddress;

    @Value("${htv.springboot.job.email-app-pass}")
    private String emailAppPassword;

    @Autowired
    private JobService jobService;

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
