package htv.springboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfiguration {

    @Bean
    public SimpleAsyncTaskExecutor virtualTaskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setVirtualThreads(true);
        taskExecutor.setConcurrencyLimit(5);
        return taskExecutor;
    }
}
