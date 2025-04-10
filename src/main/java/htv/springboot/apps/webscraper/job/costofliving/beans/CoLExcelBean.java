package htv.springboot.apps.webscraper.job.costofliving.beans;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Row;
import htv.springboot.beans.ExcelBean;

import static htv.springboot.utils.FileUtils.getExcelCellStr;

@Getter
@Setter
public class CoLExcelBean extends ExcelBean {
    private String county;
    private String stateCode;
    private CoLCostBean monthlyCost;
    private CoLCostBean yearlyCost;
    private double medianFamilyIncome;

    @Override
    public void parse(Row cells) {
        stateCode = getExcelCellStr(cells, 0);
        county = getExcelCellStr(cells, 1);

        monthlyCost = new CoLCostBean();
        monthlyCost.setHousing(cells.getCell(2).getNumericCellValue());
        monthlyCost.setFood(cells.getCell(3).getNumericCellValue());
        monthlyCost.setTransportation(cells.getCell(4).getNumericCellValue());
        monthlyCost.setHealthcare(cells.getCell(5).getNumericCellValue());
        monthlyCost.setOtherNecessities(cells.getCell(6).getNumericCellValue());
        monthlyCost.setChildCare(cells.getCell(7).getNumericCellValue());
        monthlyCost.setTaxes(cells.getCell(8).getNumericCellValue());
        monthlyCost.setTotal(cells.getCell(9).getNumericCellValue());

        yearlyCost = new CoLCostBean();
        yearlyCost.setHousing(cells.getCell(10).getNumericCellValue());
        yearlyCost.setFood(cells.getCell(11).getNumericCellValue());
        yearlyCost.setTransportation(cells.getCell(12).getNumericCellValue());
        yearlyCost.setHealthcare(cells.getCell(13).getNumericCellValue());
        yearlyCost.setOtherNecessities(cells.getCell(14).getNumericCellValue());
        yearlyCost.setChildCare(cells.getCell(15).getNumericCellValue());
        yearlyCost.setTaxes(cells.getCell(16).getNumericCellValue());
        yearlyCost.setTotal(cells.getCell(17).getNumericCellValue());

        medianFamilyIncome = cells.getCell(18).getNumericCellValue();
    }
}