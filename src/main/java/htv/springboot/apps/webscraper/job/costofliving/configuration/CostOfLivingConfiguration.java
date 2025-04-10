package htv.springboot.apps.webscraper.job.costofliving.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import htv.springboot.apps.webscraper.job.costofliving.beans.CityCountyExcelBean;
import htv.springboot.apps.webscraper.job.costofliving.beans.CoLExcelBean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static htv.springboot.utils.FileUtils.readExcelData;

@Configuration
public class CostOfLivingConfiguration {

    @Bean
    public Map<String, Map<String, CoLExcelBean>> stateCityCoLMap()
            throws IOException, URISyntaxException, NoSuchMethodException {
        Map<String, Map<String, CoLExcelBean>> stateCountyCoLMap = countyCoLMap();
        Map<String, Map<String, String>> stateCityCountyMap = cityCountyMap();

        Map<String, Map<String, CoLExcelBean>> stateCityCoLMap = new HashMap<>();
        for (String stateAbbrev : stateCityCountyMap.keySet()) {
            Map<String, String> cityCountyMap = stateCityCountyMap.get(stateAbbrev);
            for (String city : cityCountyMap.keySet()) {
                String curCityCounty = cityCountyMap.get(city);
                if (stateCountyCoLMap.containsKey(stateAbbrev)
                        && stateCountyCoLMap.get(stateAbbrev).containsKey(curCityCounty)) {
                    if (!stateCityCoLMap.containsKey(stateAbbrev)) {
                        stateCityCoLMap.put(stateAbbrev, new HashMap<>());
                    }

                    stateCityCoLMap.get(stateAbbrev).put(city, stateCountyCoLMap.get(stateAbbrev).get(curCityCounty));
                }
            }
        }

        return stateCityCoLMap;
    }

    public Map<String, Map<String, String>> cityCountyMap()
            throws IOException, URISyntaxException, NoSuchMethodException {
        Map<String, Map<String, String>> cityCountyMap = new HashMap<>();
        for (CityCountyExcelBean bean : readExcelData("webscraper/hiringcafe/citytocounty.xls", CityCountyExcelBean.class)) {
            if (!cityCountyMap.containsKey(bean.getStateCode())) {
                cityCountyMap.put(bean.getStateCode(), new HashMap<>());
            }

            String[] cities = bean.getCity().split(",");
            for (String city : cities) {
                cityCountyMap.get(bean.getStateCode()).put(city.trim(), bean.getCounty());
            }
        }

        return cityCountyMap;
    }

    public Map<String, Map<String, CoLExcelBean>> countyCoLMap()
            throws IOException, URISyntaxException, NoSuchMethodException {
        Map<String, Map<String, CoLExcelBean>> colMap = new HashMap<>();
        for (CoLExcelBean bean : readExcelData("webscraper/hiringcafe/col_2025.xlsx", CoLExcelBean.class)) {
            if (!colMap.containsKey(bean.getStateCode())) {
                colMap.put(bean.getStateCode(), new HashMap<>());
            }

            colMap.get(bean.getStateCode()).put(bean.getCounty(), bean);
        }

        return colMap;
    }
}
