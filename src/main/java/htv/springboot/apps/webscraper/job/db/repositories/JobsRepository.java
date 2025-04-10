package htv.springboot.apps.webscraper.job.db.repositories;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import htv.springboot.apps.webscraper.job.beans.Job;

import java.time.Instant;
import java.util.Optional;

public interface JobsRepository extends CrudRepository<Job, String> {
    @Query("select totimestamp(todate(writetime(company_id))) from jobs where job_id=?0")
    Optional<Instant> getLastUpdateDatetime(String jobId);
}
