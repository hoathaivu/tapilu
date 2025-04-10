package htv.springboot.utils;

public class StringUtils {
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static String windowsFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
