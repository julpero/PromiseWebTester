import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class Player implements Runnable {
    public WebDriver driver;

    public WebDriverWait shortWait;
    public WebDriverWait wait;
    public WebDriverWait longWait;

    public TestPlayer testPlayer;

    public Player(TestPlayer testPlayer) {
        this.testPlayer = testPlayer;

        initWebDriverToChrome();

        try {
            driver.get("http://localhost:3000");
            System.out.println("PLAYER "+this.testPlayer.username+" init");
            if (testPlayer.username == "Testaaja") {
                // create game
                driver.findElement(By.id("openCreateGameFormButton")).click();
                shortWait.until(visibilityOfElementLocated(By.id("newGameMyName"))).isEnabled();
                driver.findElement(By.id("newGameMyName")).sendKeys(testPlayer.username);
                driver.findElement(By.id("password1")).sendKeys(testPlayer.password);
                driver.findElement(By.id("password2")).sendKeys(testPlayer.password);
                WebElement createButton = shortWait.until(presenceOfElementLocated(By.id("createNewGameButton")));
                Actions actions = new Actions(driver);
                actions.moveToElement(createButton);
                createButton.click();
                System.out.println("PLAYER "+this.testPlayer.username+" created game");
            } else {
                // join game
                boolean joinReady = false;
                driver.findElement(By.id("openJoinGameDialogButton")).click();
                wait.until(visibilityOfElementLocated(By.className("gameContainerRow")));
                List<WebElement> games = driver.findElements(By.className("gameContainerRow"));
                for (int i = games.size() - 1; i >= 0; i--) {
                    WebElement game = games.get(i);
                    List<WebElement> listItems = game.findElements(By.className("player-in-game-item"));
                    for (int j = 0; j < listItems.size(); j++) {
                        if (listItems.get(j).getText() == "Testaaja") {
                            // join in this game
                            game.findElement(By.className("newGameMyNameInput")).sendKeys(testPlayer.username);
                            game.findElement(By.className("newGameMyPass1")).sendKeys(testPlayer.password);
                            game.findElement(By.className("newGameMyPass2")).sendKeys(testPlayer.password);
                            driver.findElement(By.className("joinThisGameButton")).click();
                            joinReady = true;
                            break;
                        }
                    }
                    if (joinReady) break;;
                }
                System.out.println("PLAYER "+this.testPlayer.username+" joined game");
            }
        } catch (Throwable t) {
            takeScreenshot("INIT_ERROR_"+this.testPlayer.username);
            throw t;
        }
    }

    public void run() {
        System.out.println("PLAYER "+this.testPlayer.username+" starts to play");
        takeScreenshot("GAME_ON_"+this.testPlayer.username);
        for (int i = 0; i < 19; i++) {
            boolean promiseOk = false;
            boolean cardOk = false;
            longWait.until(visibilityOfElementLocated(By.className("validPromiseButton")));
            takeScreenshot("ROUND_"+i+"_PROMISE_"+this.testPlayer.username);
            List<WebElement> promiseButtons = driver.findElements(By.className("validPromiseButton"));
            int rand = ThreadLocalRandom.current().nextInt(0, promiseButtons.size());
            promiseButtons.get(rand).click();
            System.out.println("round "+i+", player "+this.testPlayer.username+" promised "+promiseButtons.get(rand).getText());
            promiseOk = true;
            break;
        }
    }

    private void initWaits() {
        shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        longWait = new WebDriverWait(driver, Duration.ofSeconds(60));
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
}
