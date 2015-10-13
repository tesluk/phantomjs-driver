package com.mi;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by andrey.tesluk on 13.10.2015.
 */
public class Main {

    private static PhantomJSDriverService phantomJSDriverService;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Start");

        String propsPath = "driver.conf";
        if (args.length > 0) {
            propsPath = args[0];
            System.out.println("Load settings from file " + propsPath);
        } else {
            System.out.println("Use default setting file: " + propsPath);
        }

        Settings settings = Settings.load(propsPath);

        PhantomJSDriver webDriver = initDriver(settings);

        getData(webDriver, settings);

        phantomJSDriverService.stop();
    }

    private static PhantomJSDriver initDriver(Settings settings) {
        System.out.println("Init web driver (phantomJS)");

        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setJavascriptEnabled(true);
        cap.setCapability("takesScreenshot", true);

        if (settings.username != null) {
            System.out.println("Set username: " + settings.username);
            cap.setCapability("phantomjs.page.settings.userName", settings.username);
        }

        if (settings.password != null) {
            System.out.println("Set password: " + settings.password);
            cap.setCapability("phantomjs.page.settings.password", settings.password);
        }

        if (settings.userAgent != null) {
            System.out.println("Set User-Agent: " + settings.userAgent);
            cap.setBrowserName(settings.userAgent);
        }

        System.out.println("Path to PhantomJS executable: " + settings.pathToPhantomJS);
        cap.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
            settings.pathToPhantomJS);

        phantomJSDriverService = new PhantomJSDriverService.Builder()
            .usingPhantomJSExecutable(new File(settings.pathToPhantomJS)).build();

        PhantomJSDriver driver = new PhantomJSDriver(phantomJSDriverService, cap);
        System.out.println("PhantomJS version: " + driver.getCapabilities().getVersion());

        System.out.println("Driver ready");
        return driver;
    }

    private static void getData(PhantomJSDriver driver, Settings settings)
        throws InterruptedException, IOException {
        System.out.println("Open page: " + settings.url);
        driver.get(settings.url);

        int attempt = 0;

        boolean pageReady = false;
        while (!pageReady) {
            Thread.sleep(500);

            List<WebElement> elems = driver.findElementsByCssSelector("article#content article");

            System.out.println("Source length: " + driver.getPageSource().length());

            if (attempt % 20 == 0 && settings.output != null) {
                // Save screen shot
                String tmpPath = String.format("%s%ssnap%s", settings.output,
                    (settings.output.endsWith("/") ? "" : "/"),
                    new SimpleDateFormat("hh-mm-ss").format(new Date()));

                System.out.println("Save debug info to " + tmpPath);

                savePNG(driver, tmpPath);
                saveSource(driver, tmpPath);
            }
            attempt++;

            if (attempt > 100) {
                pageReady = true;
            }

            if (elems.isEmpty()) {
                continue;
            }
            pageReady = true;
            System.out.println("Waiting for visualization update");
            Thread.sleep(5000);
        }

        // Save result
        String resPath = String
            .format("%s%sres%s", settings.output, (settings.output.endsWith("/") ? "" : "/"),
                new SimpleDateFormat("hh-mm-ss").format(new Date()));

        savePNG(driver, resPath);
        saveSource(driver, resPath);

        System.out.println("Result saved");
    }

    private static void savePNG(PhantomJSDriver driver, String path) throws IOException {
        File tmpScreenShot = driver.getScreenshotAs(OutputType.FILE);
        IOUtils.copy(new FileInputStream(tmpScreenShot), new FileOutputStream(path + ".png"));
    }

    private static void saveSource(PhantomJSDriver driver, String path) throws IOException {
        String source = driver.getPageSource();
        IOUtils.write(source, new FileOutputStream(path + ".txt"));
    }
}
