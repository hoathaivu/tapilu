package htv.springboot.apps.webscraper.job.db.repositories;

import org.springframework.data.repository.CrudRepository;
import htv.springboot.apps.webscraper.job.beans.AppliedJob;

public interface JobsAppliedRepository extends CrudRepository<AppliedJob, String> {}
