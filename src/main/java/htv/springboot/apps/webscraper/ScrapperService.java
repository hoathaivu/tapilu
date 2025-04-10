package htv.springboot.apps.webscraper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Service
public abstract class ScrapperService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private RestClient restClient;

    protected Document retrievePage(String url) throws IOException {
        try {
            return Jsoup.connect(url).get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 429) {
                LOGGER.trace("Received 429 jobAppliedStatus code for {}. Sleeping for 1s.", url);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOGGER.error(ex);
                    Thread.currentThread().interrupt();
                }
                LOGGER.trace("Woke up. Trying again: {}", url);

                return retrievePage(url);
            }

            throw e;
        }
    }

    protected String retrieveResponse(String url, HttpMethod method, String body) {
        return restClient
                .method(method)
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .retrieve()
                .body(String.class);
    }
}
