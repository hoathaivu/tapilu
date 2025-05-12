package htv.springboot.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import htv.springboot.beans.ExcelBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileUtils {

    public static File getResourceFile(String resourcePath) throws URISyntaxException {
        return new File(ClassLoader.getSystemResource(resourcePath).toURI());
    }

    public static String getResourceContent(String resourcePath) throws URISyntaxException, IOException {
        return org.apache.commons.io.FileUtils.readFileToString(getResourceFile(resourcePath), StandardCharsets.UTF_8);
    }

    public static <T extends ExcelBean> List<T> readExcelData(String resourcePath, Class<T> clazz)
            throws IOException, NoSuchMethodException, URISyntaxException {
        List<T> dataList = new ArrayList<>();

        Workbook workbook;
        FileInputStream fis = new FileInputStream(getResourceFile(resourcePath));
        if (resourcePath.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(fis);
        } else if(resourcePath.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(fis);
        } else {
            throw new IOException("Invalid Excel file's extension: " + resourcePath);
        }
        workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);

            for (Row cells : sheet) {
                try {
                    T dataBean = clazz.getDeclaredConstructor().newInstance();
                    dataBean.parse(cells);
                    dataList.add(dataBean);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return dataList;
    }

    public static String getExcelCellStr(Row cells, int i) {
        Cell cell = cells.getCell(i);
        return cell.getCellType() == CellType.STRING ?
                cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue());
    }

    public static void mergeFiles(String dirPath, String mergedFileName, String... fileNames) throws IOException {
        File dir = new File(dirPath);
        if (dir.isDirectory() && fileNames.length > 0) {
            Set<String> mergedFileNames = new HashSet<>();

            try (OutputStream out = Files.newOutputStream(
                    Paths.get(dir.getPath(), mergedFileName),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                for (String fileName : fileNames) {
                    if (mergedFileNames.contains(fileName)) {
                        continue;
                    }

                    Files.copy(Paths.get(dir.getPath(), fileName), out);
                    mergedFileNames.add(fileName);
                }
            }
        }
    }
}
