package htv.springboot.apps.travelplan.beans;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TravelPlanPreference {
    private boolean avoidFerries;
    private boolean avoidHighway;
    private boolean avoidIndoor;
    private boolean avoidToll;
}
