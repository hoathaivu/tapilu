package htv.springboot.apps.travelplan.beans;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import htv.springboot.apps.travelplan.beans.enums.TravelMode;

import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
public class TravelPlan {
    @NotNull
    private Location from;
    @NotNull
    private Location to;
    @NotNull
    private TravelMode travelMode;
    @NotNull
    private OffsetDateTime departureTime;
    @NotNull
    private OffsetDateTime arrivalTime;
    private TravelPlanPreference travelPlanPreference;
}
