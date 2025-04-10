package htv.springboot.apps.travelplan.google.helpers;

import com.google.api.gax.grpc.GrpcCallContext;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.maps.routing.v2.*;
import com.google.protobuf.Timestamp;
import com.google.type.LatLng;
import htv.springboot.apps.travelplan.beans.TravelPlan;
import htv.springboot.apps.travelplan.beans.TravelPlanPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static htv.springboot.apps.travelplan.beans.constants.TravelPlanConstants.USER_COUNTRY;

public class StaticHelper {

    public static <RequestT, ResponseT> ResponseT callWithHeader(
            UnaryCallable<RequestT, ResponseT> callable,
            RequestT request,
            String headerKey,
            String headerVal) {
        return callable.call(
                request,
                GrpcCallContext.createDefault().withExtraHeaders(Map.of(headerKey, List.of(headerVal))));
    }

    public static ComputeRoutesRequest getComputeRoutesRequest(TravelPlan travelPlan) {
        ComputeRoutesRequest.Builder requestBuilder = ComputeRoutesRequest.newBuilder();

        requestBuilder.setOrigin(Waypoint
                .newBuilder()
                .setLocation(com.google.maps.routing.v2.Location
                        .newBuilder()
                        .setLatLng(LatLng
                                .newBuilder()
                                .setLatitude(travelPlan.getFrom().getLatitude())
                                .setLongitude(travelPlan.getFrom().getLongitude()))));
        requestBuilder.setDestination(Waypoint
                .newBuilder()
                .setLocation(com.google.maps.routing.v2.Location
                        .newBuilder()
                        .setLatLng(LatLng
                                .newBuilder()
                                .setLatitude(travelPlan.getTo().getLatitude())
                                .setLongitude(travelPlan.getTo().getLongitude()))));
        requestBuilder.addAllIntermediates(new ArrayList<>());
        requestBuilder.setTravelMode(RouteTravelMode.DRIVE);
        requestBuilder.setRoutingPreference(RoutingPreference.TRAFFIC_AWARE_OPTIMAL);
        requestBuilder.setPolylineQuality(PolylineQuality.OVERVIEW);
        requestBuilder.setPolylineEncoding(PolylineEncoding.ENCODED_POLYLINE);
        requestBuilder.setDepartureTime(Timestamp
                .newBuilder()
                .setSeconds(travelPlan.getDepartureTime().toEpochSecond())
                .setNanos(travelPlan.getDepartureTime().getNano())
                .build());
        requestBuilder.setArrivalTime(Timestamp
                .newBuilder()
                .setSeconds(travelPlan.getArrivalTime().toEpochSecond())
                .setNanos(travelPlan.getArrivalTime().getNano())
                .build());
        requestBuilder.setComputeAlternativeRoutes(true);
        TravelPlanPreference preference = travelPlan.getTravelPlanPreference();
        requestBuilder.setRouteModifiers(RouteModifiers
                .newBuilder()
                .setAvoidFerries(preference.isAvoidFerries())
                .setAvoidHighways(preference.isAvoidHighway())
                .setAvoidIndoor(preference.isAvoidIndoor())
                .setAvoidTolls(preference.isAvoidToll())
                .build());
        requestBuilder.setLanguageCode(USER_COUNTRY.getLanguage());
        requestBuilder.setRegionCode(USER_COUNTRY.getCountry());
        requestBuilder.setUnits(Units.IMPERIAL);
        requestBuilder.setOptimizeWaypointOrder(true);
        requestBuilder.addAllRequestedReferenceRoutes(new ArrayList<>());
        requestBuilder.addExtraComputations(ComputeRoutesRequest.ExtraComputation.TOLLS);
        requestBuilder.setTrafficModel(TrafficModel.BEST_GUESS);
        requestBuilder.setTransitPreferences(TransitPreferences.getDefaultInstance());

        return requestBuilder.build();
    }
}
