package htv.springboot.apps.webscraper.job.costofliving;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import htv.springboot.enums.State;
import htv.springboot.apps.webscraper.job.costofliving.beans.CoLExcelBean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class CostOfLivingService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    @Qualifier("stateCityCoLMap")
    private Map<String, Map<String, CoLExcelBean>> stateCityCoLMap;

    public Map<String, CoLExcelBean> getCoL(String[] locations) {
        Map<String, CoLExcelBean> coLMap = new HashMap<>();
        for (String location : locations) {
            coLMap.putAll(getCoL(location));
        }

        return coLMap;
    }

    public Map<String, CoLExcelBean> getCoL(String location) {
        Map<String, CoLExcelBean> coLMap = new HashMap<>();
        for (String[] locationComponents : retrieveLocationComponents(location)) {
            String city = locationComponents[0], state = locationComponents[1];
            CoLExcelBean bean = getCoL(city, state);
            if (bean != null) {
                coLMap.put(String.format("%s,%s", city, state), bean);
            }
        }

        return coLMap;
    }

    private CoLExcelBean getCoL(String city, String state) {
        String stateAbbrev = State.valueOfName(state).getAbbreviation();
        if (stateCityCoLMap.containsKey(stateAbbrev) && stateCityCoLMap.get(stateAbbrev).containsKey(city)) {
            CoLExcelBean bean = stateCityCoLMap.get(stateAbbrev).get(city);
            LOGGER.trace("Data for {}, {} found: {}", city, state, bean.getCounty());
            return bean;
        } else {
            LOGGER.trace("Data for {}, {} not found", city, state);
        }

        return null;
    }

    /**
     * Following cases will return a result:
     * - Miami,Florida
     * - Miami, Florida
     * - Florida, US
     * - Miami, Florida, US
     * - Miami/Fort Laudedale, Florida, US
     */
    private Set<String[]> retrieveLocationComponents(String location) {
        String[] components = location.split("\\s*,\\s*");

        if (components.length > 1) {
            Set<String[]> componentsSet = new HashSet<>();
            for (String city : components[0].split("/")) {
                componentsSet.add(new String[] {city, components[1]});
            }

            return componentsSet;
        } else {
            LOGGER.trace("Cannot retrieve location's components for {}", location);
            return new HashSet<>();
        }
    }
}
