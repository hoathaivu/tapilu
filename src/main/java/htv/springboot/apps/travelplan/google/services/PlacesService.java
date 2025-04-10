package htv.springboot.apps.travelplan.google.services;

import com.google.maps.places.v1.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import htv.springboot.apps.travelplan.google.helpers.StaticHelper;

import java.io.IOException;

import static htv.springboot.apps.travelplan.beans.constants.TravelPlanConstants.USER_COUNTRY;

@Service
@Validated
public class PlacesService {

    @Autowired
    private PlacesSettings placesSettings;

    public Place getPlaceById(@NotBlank String placeId) throws IOException {
        try (PlacesClient placesClient = PlacesClient.create(placesSettings)) {
            return StaticHelper.callWithHeader(
                    placesClient.getPlaceCallable(),
                    GetPlaceRequest
                            .newBuilder()
                            .setName(PlaceName.of(placeId).toString())
                            .setLanguageCode(USER_COUNTRY.getLanguage())
                            .setRegionCode(USER_COUNTRY.getCountry())
                            .build(),
                    "X-Goog-FieldMask",
                    "id,displayName"
            );
        }
    }

    public SearchTextResponse getPlaceByText(@NotBlank String addressStr) throws IOException {
        try (PlacesClient placesClient = PlacesClient.create(placesSettings)) {
            return StaticHelper.callWithHeader(
                    placesClient.searchTextCallable(),
                    SearchTextRequest
                            .newBuilder()
                            .setTextQuery(addressStr)
                            .setLanguageCode(USER_COUNTRY.getLanguage())
                            .setRegionCode(USER_COUNTRY.getCountry())
                            .build(),
                    "X-Goog-FieldMask",
                    "places.id,places.displayName,places.formattedAddress"
            );
        }
    }
}
