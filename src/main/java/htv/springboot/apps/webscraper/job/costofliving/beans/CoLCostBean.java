package htv.springboot.apps.webscraper.job.costofliving.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoLCostBean {
    private double housing;
    private double food;
    private double transportation;
    private double healthcare;
    private double otherNecessities;
    private double childCare;
    private double taxes;
    private double total;
}
