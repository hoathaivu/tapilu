package htv.springboot.apps.webscraper.job.linkedin;

import htv.springboot.apps.webscraper.BaseScrapper;
import htv.springboot.apps.webscraper.job.JobScraper;
import htv.springboot.apps.webscraper.job.beans.Job;
import htv.springboot.apps.webscraper.job.beans.JobDetail;
import htv.springboot.apps.webscraper.job.enums.JobType;
import htv.springboot.enums.ChronoWorkUnit;
import htv.springboot.utils.StringUtils;
import htv.springboot.utils.TimeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static htv.springboot.utils.TimeUtils.SHORTHAND_CHRONO_UNIT_MAP;

@Component
public class LinkedInScraper extends BaseScrapper implements JobScraper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BASE_HOSTNAME = "https://www.linkedin.com";
    //info about LinkedIn's guest urls: https://gist.github.com/Diegiwg/51c22fa7ec9d92ed9b5d1f537b9e1107
    private static final String JOBS_REQUEST_URL = BASE_HOSTNAME
            + "/jobs/search/?f_E=3&f_JT=F,C&f_T=9&f_TPR=r43200&geoId=103644278&keywords=java";
    private static final String JOB_DETAIL_REQUEST_URL = BASE_HOSTNAME + "/jobs/view/%s";
    private static final Pattern JOB_APPLY_URL_PATTERN = Pattern.compile("(?<=\\?url=)[^\"]+");

    public List<Job> retrieveJobs() throws IOException {
        Document response = retrievePage(JOBS_REQUEST_URL);

        List<Job> jobList = new ArrayList<>();
        for (Element element : response.body().select("ul[class*=jobs-search__results-list] > li")) {
            try {
                Job job = new Job();
                job.setJobId(element
                        .expectFirst("[class*=base-card]")
                        .attr("data-entity-urn")
                        .split(":")[3]);
                job.setJobTitle(element.expectFirst("[class*=base-search-card__title]").text().trim());
                job.setCompanyId(element.expectFirst("[class*=base-search-card__subtitle]").text().trim());
                job.setSource(getJobSiteName());

                parseJobPostedDate(job, element);
                parseJobDetail(job, element);

                jobList.add(job);
            } catch (Exception e) {
                LOGGER.error("Failed to parse JsonElement:\n{}", element, e);
            }
        }

        return jobList;
    }

    private void parseJobPostedDate(Job job, Element jobElement) {
        Element postedDateElement;
        if ((postedDateElement = jobElement.selectFirst("time[class*=job-search-card__listdate]")) != null) {
            LocalDate postedDate = LocalDate
                    .parse(postedDateElement.attr("datetime"), DateTimeFormatter.ISO_DATE);

            OffsetDateTime postedOffsetDate = postedDate
                    .atStartOfDay()
                    .atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now()));

            if (postedDate.isEqual(LocalDate.now())) {
                String[] timeComponents = postedDateElement.text().split(" +");

                if (timeComponents.length == 3 && SHORTHAND_CHRONO_UNIT_MAP.containsKey(timeComponents[1])) {
                    postedOffsetDate = LocalDateTime
                            .now()
                            .minus(Long.parseLong(timeComponents[0]),
                                    SHORTHAND_CHRONO_UNIT_MAP.get(timeComponents[1]).getChronoUnit())
                            .atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
                }
            }

            job.setPostedDatetime(postedOffsetDate);
        }
    }

    private void parseJobDetail(Job job, Element jobElement) throws IOException, ParseException {
        String jobDetailUrl = String.format(JOB_DETAIL_REQUEST_URL, job.getJobId());
        Element jobDetailElement = retrievePage(jobDetailUrl).body();
        try {
            JobDetail jobDetail = new JobDetail();
            jobDetail.setJobId(job.getJobId());
            jobDetail.setJobType(JobType.getEnum(jobDetailElement
                    .expectFirst("[class*=description__job-criteria-subheader]:contains(Employment type) "
                            + "+ [class*=description__job-criteria-text description__job-criteria-text--criteria]")
                    .text()
                    .trim()));

            Element jobLocationElement;
            if ((jobLocationElement = jobElement.selectFirst("[class*=job-search-card__location]")) != null) {
                jobDetail.setJobLocation(jobLocationElement.text().trim());
            } else {
                LOGGER.trace("Job location not found");
            }

            jobDetail.setJobDescription(StringUtils.getPlainText(jobDetailElement
                    .expectFirst("div[class*=core-section-container__content] div[class*=show-more-less-html__markup]")
                    .text()
                    .trim()
            ));

            parseSalary(jobDetail, jobDetailElement);

            job.setJobDetail(jobDetail);

            Element jobUrlElement;
            boolean applyUrlFound = false;
            if ((jobUrlElement = jobDetailElement.selectFirst("code[id*=applyUrl]")) != null) {
                Matcher m = JOB_APPLY_URL_PATTERN.matcher(jobUrlElement.html());
                if (m.find()) {
                    applyUrlFound = true;
                    job.setJobUrl(URLDecoder.decode(m.group(), Charset.defaultCharset()));
                } else {
                    LOGGER.trace("applyUrl's pattern not found in element: {}", jobUrlElement.html());
                }
            }

            if (!applyUrlFound) {
                job.setJobUrl(jobDetailUrl);
            }
        } catch (ValidationException | ParseException e) {
            LOGGER.error("Failed to parse JobDetail JsonElement:\n{}", jobDetailElement);
            throw e;
        }
    }

    private void parseSalary(JobDetail jobDetail, Element element) throws ParseException {
        Element salaryElement;
        if ((salaryElement = element.selectFirst("div[class*=salary compensation__salary]")) != null) {
            String[] salaryComponents = salaryElement.text().split(" *- *");
            if (salaryComponents.length <= 2) {
                jobDetail.setCompensationMin(parseSalaryStr(salaryComponents[0]));
                jobDetail.setCompensationMax(parseSalaryStr(salaryComponents[salaryComponents.length - 1]));
            }
        }
    }

    private double parseSalaryStr(String salaryComponent) throws ParseException {
        salaryComponent = salaryComponent.trim();
        if (!salaryComponent.isEmpty() && !Character.isDigit(salaryComponent.charAt(0))) {
            salaryComponent = salaryComponent.substring(1);
        }

        if (salaryComponent.isEmpty()) {
            return 0;
        }

        int slashPos = salaryComponent.indexOf("/");
        double salary = NumberFormat.getInstance(Locale.getDefault())
                .parse(salaryComponent.substring(0, slashPos == -1 ? salaryComponent.length() : slashPos))
                .doubleValue();
        if (slashPos != -1) {
            String timeShorthand = salaryComponent.substring(slashPos + 1).trim();
            try {
                salary = TimeUtils.convert(salary, timeShorthand, ChronoWorkUnit.YEARS);
            } catch (UnsupportedOperationException e) {
                LOGGER.trace("The shorthand {} is not supported", timeShorthand);
            }
        }

        return salary;
    }

    @Override
    public String getJobSiteName() {
        return "LinkedIn";
    }
}
