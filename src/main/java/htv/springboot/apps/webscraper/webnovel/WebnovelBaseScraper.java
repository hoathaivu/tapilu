package htv.springboot.apps.webscraper.webnovel;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.jsoup.nodes.Document;
import htv.springboot.apps.webscraper.BaseScrapper;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static htv.springboot.utils.StringUtils.windowsFileName;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
public abstract class WebnovelBaseScraper extends BaseScrapper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DEST_DIRECTORY = "C:/Users/hthvu/Documents/MEGA Downloads";

    protected String hostName;

    public WebnovelBaseScraper(String hostName) {
        this.hostName = hostName;
    }

    public String[] retrieveVolumesUrls(String url) throws IOException {
        LOGGER.trace("Retrieving volumes' URLs for {}", url);
        return getStoryVolumesUrl(retrievePage(url));
    }

    public void process(String url) throws IOException {
        //testParseLogic(url);
        writeToFile(url);
        LOGGER.info("Finished processing {}", url);
    }

    protected abstract String getStoryName(Document response);

    protected abstract String getChapterContent(Document response);

    protected abstract String getNextChapterUrl(Document response);

    protected String[] getStoryVolumesUrl(Document response) {
        return new String[] {response.location()};
    }

    private void testParseLogic(String url) throws IOException {
        do {
            Document document = retrievePage(url);

            LOGGER.trace(() -> new ParameterizedMessage("Story name: {}", getStoryName(document)));
            LOGGER.trace("Getting chapter's content");
            LOGGER.trace(() -> new ParameterizedMessage("Text:{}", getChapterContent(document)));
            LOGGER.trace("Getting next chapter's URL");
            url = getNextChapterUrl(document);
        } while (!isBlank(url));
    }

    private void writeToFile(String url) throws IOException {
        StringBuilder content = new StringBuilder();

        Document document;
        do {
            document = retrievePage(url);

            LOGGER.trace("Getting chapter's content");
            content.append(getChapterContent(document));

            LOGGER.trace("Getting next chapter's URL");
            url = getNextChapterUrl(document);
        } while (!isBlank(url));

        File txtFile = new File(DEST_DIRECTORY + "/" + windowsFileName(getStoryName(document)) + ".txt");
        LOGGER.trace("Prepare output file: {}", txtFile.getAbsolutePath());
        txtFile.createNewFile();

        try (PrintWriter pw = new PrintWriter(new FileOutputStream(txtFile, true), true, UTF_8)) {
            LOGGER.trace("Writing to output file");
            pw.append(content.toString().trim());
        }
        LOGGER.trace("Finished writing to output file");
    }
}
