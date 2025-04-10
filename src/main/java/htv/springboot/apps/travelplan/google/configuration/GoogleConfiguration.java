package htv.springboot.apps.travelplan.google.configuration;

import com.google.api.gax.retrying.RetrySettings;
import com.google.maps.GeoApiContext;
import com.google.maps.places.v1.PlacesSettings;
import com.google.maps.routing.v2.RoutesSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GoogleConfiguration {

    @Bean
    public RoutesSettings routesSettings() throws IOException {
        RoutesSettings.Builder builder = RoutesSettings.newBuilder();
        builder.applyToAllUnaryMethods(methodBuilder -> {
            methodBuilder.setRetrySettings(
                    RetrySettings
                            .newBuilder()
                            .setMaxAttempts(1)
                            .build()
            );
            return null;
        });
        return builder.build();
    }

    @Bean
    public PlacesSettings placesSettings() throws IOException {
        PlacesSettings.Builder builder = PlacesSettings.newBuilder();
        builder.applyToAllUnaryMethods(methodBuilder -> {
            methodBuilder.setRetrySettings(
                    RetrySettings
                            .newBuilder()
                            .setMaxAttempts(1)
                            .build()
            );
            return null;
        });
        return builder.build();
    }

    @Bean
    public GeoApiContext geoApiContext() {
        return new GeoApiContext
                .Builder()
                .disableRetries()
                .build();
    }
}
