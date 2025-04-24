package htv.springboot.browsers;

import htv.springboot.browsers.firefox.FirefoxService;

public class Browsers {

    private static final Browser firefoxBrowser = new FirefoxService();

    public static void openUrl(String url) {
        firefoxBrowser.openUrl(url);
    }
}
