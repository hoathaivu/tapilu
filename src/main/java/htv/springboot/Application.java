package htv.springboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import htv.springboot.apps.webscraper.job.costofliving.CostOfLivingService;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).headless(false).run(args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext context) {
        return _ -> context.getBean(CostOfLivingService.class);
    }
}
