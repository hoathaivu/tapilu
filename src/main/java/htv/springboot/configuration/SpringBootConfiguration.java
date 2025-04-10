package htv.springboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SpringBootConfiguration {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
