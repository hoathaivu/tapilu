package htv.springboot.utils;

import com.sun.mail.imap.IMAPFolder;
import htv.springboot.apps.webscraper.job.JobService;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.FolderClosedException;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.event.ConnectionAdapter;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.search.FlagTerm;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.stream.Collectors;

public class MailUtils {
    public static final String UNSUBSCRIBE_HEADER = "List-Unsubscribe";
    public static final String UNSUBSCRIBE_POST_HEADER = "List-Unsubscribe-Post";

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String IMAPS = "imaps";
    private static final String GMAIL_IMAPS = "imap.gmail.com";
    private static final String GMAIL_SUFFIX = "@gmail.com";
    private static final String YAHOO_IMAPS = "imap.mail.yahoo.com";
    private static final String YAHOO_SUFFIX = "@yahoo.com";
    private static final String OUTLOOK_IMAPS = "imap-mail.outlook.com";
    private static final String OUTLOOK_SUFFIX = "@outlook.com";

    public static String getPlainEmailContent(Part p) throws MessagingException, IOException {
        return htv.springboot.utils.StringUtils.getPlainText(getEmailContent(p));
    }

    //copied from https://javaee.github.io/javamail/FAQ#mainbody
    public static String getEmailContent(Part p) throws MessagingException, IOException {
        LOGGER.trace("Getting email content");
        if (p.isMimeType("text/*")) {
            return (String) p.getContent();
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getEmailContent(bp);
                } else if (bp.isMimeType("text/html")) {
                    String s = getEmailContent(bp);
                    if (s != null)
                        return s;
                } else {
                    return getEmailContent(bp);
                }
            }

            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getEmailContent(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

    public static void deleteFlaggedEmails(
            String emailAddress, String appPassword, Flags.Flag flag) throws MessagingException {
        LOGGER.trace("Deleting emails with flag {} of {}", flag.hashCode(), emailAddress);
        Store store = Session.getDefaultInstance(new Properties()).getStore(IMAPS);
        store.connect(getImaps(emailAddress), emailAddress, appPassword);

        IMAPFolder inbox = (IMAPFolder) store.getFolder("Inbox");
        inbox.open(Folder.READ_WRITE);
        Folder trash = store.getFolder("Trash");
        trash.open(Folder.READ_WRITE);
        inbox.moveMessages(inbox.search(new FlagTerm(new Flags(flag), true)), trash);

        inbox.close(true);
        store.close();
    }

    public static void unsubscribeFromEmail(Message email, RestClient restClient) throws MessagingException {
        LOGGER.trace("Attempting to unsubscribe from {}",
                Arrays.stream(email.getFrom()).map(Address::toString).collect(Collectors.joining(",")));
        Enumeration<Header> enumeration = email.getAllHeaders();
        String unsubscribeUrl = "", unsubscribeHeaderVal = "";
        while (enumeration.hasMoreElements()) {
            Header header = enumeration.nextElement();
            if (UNSUBSCRIBE_HEADER.equals(header.getName())) {
                unsubscribeUrl = header.getValue();
            } else if (UNSUBSCRIBE_POST_HEADER.equals(header.getName())) {
                unsubscribeHeaderVal = header.getValue();
            }
        }

        if (!StringUtils.isBlank(unsubscribeUrl)) {
            unsubscribeUrl = unsubscribeUrl.substring(3, unsubscribeUrl.length() - 3);
            RestClient.RequestBodySpec spec = restClient
                    .post()
                    .uri(unsubscribeUrl);

            if (!StringUtils.isBlank(unsubscribeHeaderVal)) {
                String[] headerParts = unsubscribeHeaderVal.split("=");
                spec.header(headerParts[0], headerParts[1]);
            }

            spec.body(String.class);
        } else {
            LOGGER.trace("No unsubscribe header found in email's header");
        }
    }

    public static void setupIdle(String emailAddress, String appPassword, RestClient restClient)
            throws MessagingException, IOException {
        Store store = Session.getDefaultInstance(new Properties()).getStore(IMAPS);
        store.connect(getImaps(emailAddress), emailAddress, appPassword);
        IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);

        JobService.processNewEmails(emailAddress, appPassword, folder.getMessages(), restClient);

        folder.addMessageCountListener(new MessageCountAdapter() {
            public void messagesAdded(MessageCountEvent ev) {
                try {
                    JobService.processNewEmails(emailAddress, appPassword, ev.getMessages(), restClient);
                } catch (MessagingException | IOException e) {
                    LOGGER.error("Error processing new emails", e);
                }
            }
        });

        folder.addConnectionListener(new ConnectionAdapter() {
            public void closed(ConnectionEvent ce) {
                reconnect();
            }

            public void disconnected(ConnectionEvent e) {
                reconnect();
            }

            private void reconnect() {
                try {
                    LOGGER.info("Connection closed. Reconnecting to server.");

                    setupIdle(emailAddress, appPassword, restClient);
                } catch (Exception e) {
                    LOGGER.error("Could not connect to server", e);
                }
            }
        });

        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                folder.idle();
            }
        } catch (FolderClosedException e) {
            LOGGER.error("Folder closed. Should be restarted soon.", e);
        }
    }

    private static String getImaps(String emailAddress) {
        if (emailAddress.endsWith(GMAIL_SUFFIX)) {
            return GMAIL_IMAPS;
        } else if (emailAddress.endsWith(YAHOO_SUFFIX)) {
            return YAHOO_IMAPS;
        } else if (emailAddress.endsWith(OUTLOOK_SUFFIX)) {
            return OUTLOOK_IMAPS;
        } else {
            throw new RuntimeException("Unknown email: " + emailAddress);
        }
    }
}
