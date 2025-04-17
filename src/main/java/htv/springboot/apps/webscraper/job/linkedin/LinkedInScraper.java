package htv.springboot.apps.webscraper.job.linkedin;

import htv.springboot.apps.webscraper.BaseScrapper;
import htv.springboot.apps.webscraper.job.JobScraper;
import htv.springboot.apps.webscraper.job.beans.Job;
import htv.springboot.apps.webscraper.job.beans.JobDetail;
import htv.springboot.apps.webscraper.job.enums.JobType;
import htv.springboot.constants.TimeConstant;
import htv.springboot.utils.StringUtils;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LinkedInScraper extends BaseScrapper implements JobScraper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BASE_HOSTNAME = "https://www.linkedin.com";
    //info on URL: https://gist.github.com/Diegiwg/51c22fa7ec9d92ed9b5d1f537b9e1107
    private static final String JOBS_REQUEST_URL = BASE_HOSTNAME
            + "/jobs-guest/jobs/api/seeMoreJobPostings/search?"
            + "f_E=3&f_JT=F,C&f_T=9&f_TPR=r3600&keywords=software engineer&location=United States";
    private static final String JOB_DETAIL_REQUEST_URL = BASE_HOSTNAME
            + "/jobs-guest/jobs/api/jobPosting/%s";
    private static final String JOB_DETAIL_AUTHED_REQUEST_URL = BASE_HOSTNAME
            + "/jobs/search/?currentJobId=%S";
    private static final Pattern JOB_APPLY_URL_PATTERN = Pattern.compile("(?<=\\?url=)[^\"]+");

    public List<Job> retrieveJobs() throws IOException {
        Document response = retrievePage(JOBS_REQUEST_URL);

        List<Job> jobList = new ArrayList<>();
        for (Element element : response.body().select("li")) {
            try {
                Job job = new Job();
                job.setJobId(element
                        .expectFirst("[class*=base-card]")
                        .attr("data-entity-urn")
                        .split(":")[3]);
                job.setJobTitle(element.expectFirst("[class*=base-search-card__title]").text().trim());
                job.setCompanyId(element.expectFirst("[class*=base-search-card__subtitle]").text().trim());
                job.setSource(getJobSiteName());

                Element postedDateElement;
                if ((postedDateElement = element.selectFirst("time[class*=job-search-card__listdate]")) != null) {
                    job.setPostedDatetime(LocalDate
                            .parse(postedDateElement.attr("datetime"), DateTimeFormatter.ISO_DATE)
                            .atStartOfDay()
                            .atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now())));
                }

                parseJobDetail(job, element);

                jobList.add(job);
            } catch (Exception e) {
                LOGGER.error("Failed to parse JsonElement:\n{}", element, e);
            }
        }

        return jobList;
    }

    private void parseJobDetail(Job job, Element jobElement) throws IOException, ParseException {
        Element jobDetailElement = retrievePage(String.format(JOB_DETAIL_REQUEST_URL, job.getJobId())).body();
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
                job.setJobUrl(String.format(JOB_DETAIL_AUTHED_REQUEST_URL, job.getJobId()));
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
        if (salaryComponent.startsWith("$")) {
            salaryComponent = salaryComponent.substring(1);
        }

        int slashPos = salaryComponent.indexOf("/");
        double salary = NumberFormat.getInstance(Locale.getDefault())
                .parse(salaryComponent.substring(0, slashPos == -1 ? salaryComponent.length() : slashPos))
                .doubleValue();
        if (slashPos != -1) {
            String timeShorthand = salaryComponent.substring(slashPos + 1).trim();
            if (TimeConstant.SHORTHAND_MULT_MAP.containsKey(timeShorthand)) {
                salary *= TimeConstant.SHORTHAND_MULT_MAP.get(timeShorthand);
            }
        }

        return salary;
    }

    @Override
    public String getJobSiteName() {
        return "LinkedIn";
    }
}
