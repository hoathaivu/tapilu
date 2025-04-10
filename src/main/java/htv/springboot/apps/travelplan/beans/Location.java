package htv.springboot.apps.travelplan.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Location {
    private double latitude;
    private double longitude;
}
