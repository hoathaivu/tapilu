package htv.springboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
public class SchedulingConfiguration {

    @Bean
    public ThreadPoolTaskExecutor virtualTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setVirtualThreads(true);
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setMaxPoolSize(5);
        taskExecutor.setQueueCapacity(10);
        return taskExecutor;
    }
}
