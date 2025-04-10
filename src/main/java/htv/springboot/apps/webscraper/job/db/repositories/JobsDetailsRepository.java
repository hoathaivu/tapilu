package htv.springboot.apps.webscraper.job.db.repositories;

import org.springframework.data.repository.CrudRepository;
import htv.springboot.apps.webscraper.job.beans.JobDetail;

public interface JobsDetailsRepository extends CrudRepository<JobDetail, String> {}
