# tapilu

Tasks:
- Use a JS framework for notification windows
- LLM:
  - to detect an email is for reject/accept/etc
  - to map email to job
  - https://github.com/deepseek-ai/DeepSeek-V3?
- dependency conflicts
- Docker?
- Kafka for msg to display?

Features:
- Every 1 hour (configurable in `application.properties`), scrape based on implementation of `JobScraper` interface and display the scraped jobs. User can then do one of the following:
  - Skip (i.e. do nothing)
  - Proceed, in which case the job's URL will be opened in Firefox browser
  - Regardless of what is selected, the job will not reappear in subsequent runs
- Store jobs for future work in Cassandra DB
- Listen to email account given in environment variables `TAPILU_EMAIL_ADDRESS` and `TAPILU_EMAIL_APP_PASSWORD` (variables configurable in `application.properties`) and display email's content after it was received (~1min delay), accounting for different content's type (e.g. plain text, HTML). User can decide to do one of the following:
  - Skip (i.e. do nothing)
  - Delete (i.e. move the email to Trash folder)
  - Unsubscribe and Delete (if possible) (i.e. unsubscribe from email's sender and move email to Trash folder)

WSL:
- docker run -p 9042:9042 --rm --name cassandra -d cassandra:latest
- vi tables.cql
- docker cp tables.cql cassandra:tables.cql
- docker exec -it cassandra bash -c "cqlsh -u cassandra -p cassandra"
  - give it a minute or two before running, otherwise can run into following error : `Connection error: ('Unable to connect to any servers', {'127.0.0.1:9042': ConnectionRefusedError(111, "Tried connecting to [('127.0.0.1', 9042)]. Last error: Connection refused")})`

CQL:
- SOURCE 'tables.cql'


Past issues
- `com.sun.mail:jakarta.mail` vs `org.springframework.integration:spring-integration-mail`
  - `com.sun.mail:jakarta.mail`:
    - Straightforward to use
    - Easy to set low-level config/command
    - Can either call `Folder`'s `idle()` directly, or use `IdleManager`
    - In either cases, require manual monitor for IDLE in case connection timeout or server drops the connection
  - `org.springframework.integration:spring-integration-mail`:
    - Part of Spring Integration, so need to learn about [DSL](https://docs.spring.io/spring-integration/reference/dsl.html)
    - JavaMail API is hidden behind an overhead layer, so low-level action may be hard/impossible to do
    - IDLE is handled based on set of properties (e.g. `simpleContent`, `autoCloseFolder`)

Current issues
- Email's unsubscribe not working correctly