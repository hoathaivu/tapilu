package htv.springboot.utils;

import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
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

    public static Message[] getUnreadMails(String emailAddress, String appPassword) throws MessagingException {
        LOGGER.trace("Getting unread email of {}", emailAddress);
        return getMails(emailAddress, appPassword, new FlagTerm(new Flags(Flags.Flag.SEEN), false));
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

    private static Message[] getMails(String emailAddress, String appPassword, FlagTerm term) throws MessagingException {
        Store store = Session.getDefaultInstance(new Properties()).getStore(IMAPS);
        store.connect(getImaps(emailAddress), emailAddress, appPassword);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        return inbox.search(term);
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
