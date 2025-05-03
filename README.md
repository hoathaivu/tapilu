# tapilu

Tasks:
- Use a JS framework for notification windows
  - htmx
  - Angular
- LLM:
  - to detect job's info from description (e.g. is onsite/remote/hybrid, location)
  - to detect an email is for reject/accept/etc
  - to map email to job
  - https://github.com/deepseek-ai/DeepSeek-V3?
    - Installation: https://github.com/sgl-project/sglang/blob/main/docs/references/amd.md
    - May need 400GB (?)
- dependency conflicts
- Docker?
- Kafka for msg to display?
  - wait for response for 5min, then pause for 15min before display again

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
1. docker run -p 9042:9042 --rm --name cassandra -d cassandra:latest
2. vi tables.cql
3. docker cp tables.cql cassandra:tables.cql
4. docker exec -it cassandra bash -c "cqlsh -u cassandra -p cassandra"
   1. give it a minute or two before running, otherwise can run into following error : `Connection error: ('Unable to connect to any servers', {'127.0.0.1:9042': ConnectionRefusedError(111, "Tried connecting to [('127.0.0.1', 9042)]. Last error: Connection refused")})`

CQL:
1. SOURCE 'tables.cql'

DeepSeek:
- NVIDIA Container Toolkit (https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/install-guide.html)
  1. Install NVIDIA Driver for GPU Support using NVIDIA GeForce Game Ready
  2. Install and setup toolkit
     1. `curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg \
     && curl -s -L https://nvidia.github.io/libnvidia-container/stable/deb/nvidia-container-toolkit.list | \
       sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
       sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list`
     2. `sudo apt-get update`
     3. `sudo apt-get install -y nvidia-container-toolkit`
     4. `sudo nvidia-ctk runtime configure --runtime=docker`
     5. `sudo systemctl restart docker`
  3. Confirm installation: `sudo docker run --rm --runtime=nvidia --gpus all ubuntu nvidia-smi`
- SGLang
  1. `docker pull lmsysorg/sglang:latest`
  2. `docker run --gpus all --shm-size 32g -p 30000:30000 -v ~/.cache/huggingface:/root/.cache/huggingface --ipc=host lmsysorg/sglang:latest \
    python3 -m sglang.launch_server --model deepseek-ai/DeepSeek-V3 --tp 1 --trust-remote-code --port 30000`

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
- Occasionally, nothing was displayed when an email's body is of HTML
  - Reason: the content contains meta tag which has attributes `http-equiv`, whose value is `content-type`, and `content`, whose value is neither `text\html` nor `text\plain`
  - Fix: set JTextPane's Document's property `IgnoreCharsetDirective` to `true`, which will tell Document's Parser to ignore the meta data above 
- Sometimes salary contains non-US currency
  - This is considered data errors since we are looking for jobs in US only 

Current issues
- Email's unsubscribe not working correctly
- GPU with 8GB is not enough
  - Attempt to change [optimization config](https://docs.sglang.ai/backend/server_arguments.html#optimization) but same error
    - Failed config: `--mem-fraction-static 0.1 --chunked-prefill-size 1024 --torch-compile-max-bs 2`