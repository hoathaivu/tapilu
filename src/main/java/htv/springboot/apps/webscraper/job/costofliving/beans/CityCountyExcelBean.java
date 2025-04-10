package htv.springboot.apps.webscraper.job.costofliving.beans;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Row;
import htv.springboot.beans.ExcelBean;

import static htv.springboot.utils.FileUtils.getExcelCellStr;

@Getter
@Setter
public class CityCountyExcelBean extends ExcelBean {
    private String city;
    private String stateCode;
    private String county;

    @Override
    public void parse(Row cells) {
        city = getExcelCellStr(cells, 0) + "," + getExcelCellStr(cells, 1) + "," + getExcelCellStr(cells, 2);
        stateCode = getExcelCellStr(cells, 3);
        county = getExcelCellStr(cells, 4);
    }
}
