package htv.springboot.apps.webscraper.webnovel.scrapers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import htv.springboot.apps.webscraper.webnovel.WebnovelBaseScraper;

@Component
public class WattpadWebnovelScraper extends WebnovelBaseScraper {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String HOST_NAME = "www.wattpad.com";

    public WattpadWebnovelScraper() {
        super(HOST_NAME);
    }

    @Override
    protected String getStoryName(Document response) {
        return response
                .body()
                .expectFirst("span[class*=toc-full] > span[class*=info] > [class*=title]")
                .text();
    }

    @Override
    protected String getChapterContent(Document response) {
        StringBuilder sb = new StringBuilder();

        if (!getNextChapterUrl(response).contains("/page/")) {
            sb.append("\n");
            sb.append("\n");
            sb.append(response.body()
                            .expectFirst("ul[class*=table-of-content] > li[class*=active]")
                            .text()
                            .trim());
            sb.append("\n");
        }

        for (Element element : response.body().select("p[data-p-id]")) {
            sb.append("\n");
            sb.append(element.text().trim());
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    protected String getNextChapterUrl(Document response) {
        Element nextLink = response
                            .head()
                            .selectFirst("link[rel*=next]");

        if (nextLink != null) {
            String nextPageUrl = nextLink.attr("href");
            LOGGER.trace("Has next page: {}", nextPageUrl);
            return nextPageUrl;
        } else {
            LOGGER.trace("No next page. Get next chapter");
            nextLink = response.body().selectFirst("div[id*=story-part-navigation]");

            if (nextLink != null && nextLink.selectFirst("a") != null) {
                String nextPageUrl = nextLink.expectFirst("a").attr("href");
                LOGGER.trace("Has next chapter: {}", nextPageUrl);
                return nextPageUrl;
            } else {
                LOGGER.trace("No next chapter");
            }
        }

        return "";
    }

    @Override
    public String[] retrieveVolumesUrls(String url) {
        return new String[] {url};
    }
}
