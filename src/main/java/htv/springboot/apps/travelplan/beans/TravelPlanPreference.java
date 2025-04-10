package htv.springboot.apps.travelplan.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TravelPlanPreference {
    private boolean avoidFerries;
    private boolean avoidHighway;
    private boolean avoidIndoor;
    private boolean avoidToll;
}
