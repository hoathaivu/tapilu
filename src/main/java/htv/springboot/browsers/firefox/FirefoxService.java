package htv.springboot.browsers.firefox;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import htv.springboot.browsers.Browser;

import java.io.IOException;

@Service
public class FirefoxService implements Browser {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String FIREFOX_CMD = SystemUtils.IS_OS_WINDOWS ?
            "C:/Program Files/Mozilla Firefox/firefox.exe" : "/usr/bin/firefox";

    @Override
    public void openUrl(String url) {
        LOGGER.trace("Open in normal browser: {}", url);
        openUrl(url, false);
    }

    @Override
    public void openUrlInPrivate(String url) {
        LOGGER.trace("Open in private browser: {}", url);
        openUrl(url, true);
    }

    private void openUrl(String url, boolean inPrivate) {
        String[] cmdArr = inPrivate ?
                new String[] {FIREFOX_CMD, "--private-window", url}
                : new String[] {FIREFOX_CMD, url};

        try {
            Runtime.getRuntime().exec(cmdArr);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
}
