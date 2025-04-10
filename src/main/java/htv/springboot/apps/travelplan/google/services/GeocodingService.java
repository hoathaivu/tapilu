package htv.springboot.apps.travelplan.google.services;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.LocationType;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;

import static htv.springboot.apps.travelplan.beans.constants.TravelPlanConstants.USER_COUNTRY;

@Service
@Validated
public class GeocodingService {

    @Autowired
    private GeoApiContext geoApiContext;

    public GeocodingResult[] reverseGeocoding(@NotNull double latitude, @NotNull double longitude)
            throws IOException, InterruptedException, ApiException {
        return GeocodingApi
                .newRequest(geoApiContext)
                .latlng(new LatLng(latitude, longitude))
                .language(USER_COUNTRY.getLanguage())
                .region(USER_COUNTRY.getCountry())
                .locationType(LocationType.ROOFTOP)
                .await();
    }
}
