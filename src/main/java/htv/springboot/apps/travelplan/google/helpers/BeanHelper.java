package htv.springboot.apps.travelplan.google.helpers;

import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.places.v1.Place;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import htv.springboot.apps.travelplan.beans.Location;
import htv.springboot.apps.travelplan.google.services.GeocodingService;
import htv.springboot.apps.travelplan.google.services.PlacesService;

import java.io.IOException;

@Service
@Validated
public class BeanHelper {

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private PlacesService placesService;

    public Place getPlace(@NotNull Location location) throws IOException, InterruptedException, ApiException {
        GeocodingResult[] geocodingResults =
                geocodingService.reverseGeocoding(location.getLatitude(), location.getLongitude());

        assert geocodingResults.length >= 1 : "No location found for given coords";

        return placesService.getPlaceById(geocodingResults[0].placeId);
    }
}
