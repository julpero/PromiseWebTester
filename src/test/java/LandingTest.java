import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeOptions.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

//import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class LandingTest {
    public WebDriver driver;

    public WebDriverWait shortWait;
    public WebDriverWait wait;
    public WebDriverWait longWait;

    @Test
    public void TestLandingPage() {
        try {
            System.out.println("TestLandingPage starts");
            initWebDriverToChrome();
            driver.get("http://localhost:3000");
            final String pageTitle = driver.getTitle();
            assertEquals("promiseweb - Promise Card Game", pageTitle, "FAIL: page title not equal!");

            longWait.until(visibilityOfElementLocated(By.className("tabulator-table")));

            WebElement spurterInfo = longWait.until(visibilityOfElementLocated(By.id("spurterInfo")));
            assertTrue(spurterInfo.getText().length() > 0, "FAIL: no spurter info!");

            System.out.println("TestLandingPage SUCCESS");
        } catch (Throwable t) {
            takeScreenshot("TestLandingPage_ERROR");
            throw t;
        }

        driver.quit();
    }

    public void takeScreenshot(String pathname) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(timestamp);
        pathname = "./build/test-results/test/screenshots/" + formattedDate + "_" + pathname + ".png";
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileHandler.copy(src, new File(pathname));
        } catch (IOException i) {
            System.out.println("Screenshot error: " + i);
        }
    }

    private void initWebDriverToChrome() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1900x1200");
        options.setHeadless(true);
        try{
            driver = new ChromeDriver(options);
        } catch (final Exception e) {
            throw new RuntimeException("Problems in chrome webdriver init", e);
        }
        initWaits();
    }

    private void initWaits() {
        shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        longWait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }
}
