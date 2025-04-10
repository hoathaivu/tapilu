package htv.springboot.apps.webscraper.job.db.repositories;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import htv.springboot.apps.webscraper.job.enums.JobStatsStatus;
import htv.springboot.apps.webscraper.job.beans.JobStats;

public interface JobsStatisticsRepository extends CrudRepository<JobStats, String> {
    @Query("update jobs_statistics set job_applied_status=?0 where job_id=?1")
    void updateStatus(JobStatsStatus status, String jobId);
}
