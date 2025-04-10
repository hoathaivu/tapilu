package htv.springboot.apps.travelplan.google.services;

import com.google.maps.routing.v2.ComputeRoutesResponse;
import com.google.maps.routing.v2.RoutesClient;
import com.google.maps.routing.v2.RoutesSettings;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import htv.springboot.apps.travelplan.beans.TravelPlan;
import htv.springboot.apps.travelplan.google.helpers.StaticHelper;

import java.io.IOException;

@Service
@Validated
public class RoutesService {

    @Autowired
    private RoutesSettings routesSettings;

    public ComputeRoutesResponse computeRoute(@NotNull TravelPlan travelPlan) throws IOException {
        try (RoutesClient routesClient = RoutesClient.create(routesSettings)) {
            return StaticHelper.callWithHeader(
                    routesClient.computeRoutesCallable(),
                    StaticHelper.getComputeRoutesRequest(travelPlan),
                    "X-Goog-FieldMask",
                    "*"
            );
        }
    }
}
