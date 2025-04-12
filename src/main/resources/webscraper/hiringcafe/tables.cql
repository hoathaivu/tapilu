CREATE KEYSPACE IF NOT EXISTS spring_cassandra WITH durable_writes = true AND replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

DROP TABLE IF EXISTS spring_cassandra.jobs;
DROP TABLE IF EXISTS spring_cassandra.jobs_details;
DROP TABLE IF EXISTS spring_cassandra.jobs_applied;
DROP TABLE IF EXISTS spring_cassandra.jobs_statistics;

CREATE TABLE IF NOT EXISTS spring_cassandra.jobs (job_id text PRIMARY KEY, company_id text, posted_datetime timestamp, job_title text, job_url text);
CREATE TABLE IF NOT EXISTS spring_cassandra.jobs_details (job_id text PRIMARY KEY, job_description text, job_type text, job_location text, job_location_type text, compensation_min double, compensation_max double);
CREATE TABLE IF NOT EXISTS spring_cassandra.jobs_applied (job_id text PRIMARY KEY, applied_datetime timestamp, status text);
CREATE TABLE IF NOT EXISTS spring_cassandra.jobs_statistics (job_id text PRIMARY KEY, job_applied_status text, seen_on frozen<set<timestamp>>, job_location_col double);