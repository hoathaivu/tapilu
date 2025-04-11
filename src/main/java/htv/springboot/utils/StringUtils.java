package htv.springboot.utils;

public class StringUtils {
    public static String windowsFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
