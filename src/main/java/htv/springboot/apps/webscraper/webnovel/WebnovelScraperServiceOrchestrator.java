package htv.springboot.apps.webscraper.webnovel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class WebnovelScraperServiceOrchestrator {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, WebnovelScraperService> scrapers;

    @Autowired
    public WebnovelScraperServiceOrchestrator(List<? extends WebnovelScraperService> scrapersList) {
        scrapers = scrapersList
                .stream()
                .collect(toMap(WebnovelScraperService::getHostName, identity()));
    }

    public void processMultiple(String[] urls) throws InterruptedException {
        CountDownLatch doneSignal = new CountDownLatch(urls.length);
        try (ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.newCachedThreadPool()) {
            for (String url : urls) {
                es.execute(() -> {
                    try {
                        processByVolumes(url, es, doneSignal);
                    } catch (IOException e) {
                        LOGGER.error(e);
                    }
                });
            }

            doneSignal.await();
            LOGGER.info("Shutting down thread's executor");
            es.shutdown();

            if (es.awaitTermination(1, TimeUnit.MINUTES)) {
                LOGGER.info("Tasks finished before timeout");
            } else {
                LOGGER.info("Timeout triggered");
            }
        }
    }

    private void processByVolumes(String url, ExecutorService es, CountDownLatch doneSignal) throws IOException {
        if (isBlank(url)) {
            return;
        }

        try {
            String urlHost = new URI(url).getHost();
            if (scrapers.containsKey(urlHost)) {
                for (String volumeUrl : scrapers.get(urlHost).retrieveVolumesUrls(url)) {
                    LOGGER.info("Creating new thread for new volume of {}: {}", url, volumeUrl);
                    es.execute(() -> {
                        try {
                            processSingle(volumeUrl);
                        } catch (IOException | URISyntaxException e) {
                            LOGGER.error(e);
                        }
                    });
                }
                LOGGER.info("Finished creating threads for volumes of {}", url);
            } else {
                LOGGER.info("Unexpected host: {}", urlHost);
            }
        } catch (URISyntaxException e) {
            LOGGER.error(e);
        }

        doneSignal.countDown();
    }

    public void processSingle(String url) throws IOException, URISyntaxException {
        if (isBlank(url)) {
            return;
        }

        String urlHost = new URI(url).getHost();
        if (scrapers.containsKey(urlHost)) {
            scrapers.get(urlHost).process(url);
        } else {
            LOGGER.info("Unexpected host: {}", urlHost);
        }
    }
}
