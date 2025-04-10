package htv.springboot.beans;

import org.apache.poi.ss.usermodel.Row;

public abstract class ExcelBean {
    public abstract  void parse(Row cells);
}
