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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

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

    @Test
    public void TestReportPage() {
        try {
            System.out.println("TestReportPage starts");
            initWebDriverToChrome();
            driver.get("http://localhost:3000/gameReport.html");
            final String pageTitle = driver.getTitle();
            assertEquals("promiseweb - Promise Card Game - game reports", pageTitle, "FAIL: page title not equal!");

            longWait.until(elementToBeClickable(By.id("oneGameReportButton")));
            wait.until(elementToBeClickable(By.id("averageGameReportButton")));

            driver.findElement(By.id("oneGameReportButton")).click();
            longWait.until(visibilityOfElementLocated(By.className("game-container-div")));
            List<WebElement> gameContainerDivs = driver.findElements(By.className("game-container-div"));
            assertTrue(gameContainerDivs.size() > 0, "FAIL: no game container divs!");

            gameContainerDivs.get(0).findElement(By.className("reportGameButton")).click();
            wait.until(visibilityOfElementLocated(By.id("oneGameReportModalLabel")));
            driver.findElement(By.className("btn-close")).click();

            driver.findElement(By.id("averageGameReportButton")).click();
            longWait.until(visibilityOfElementLocated(By.id("gamesPlayedReportCanvas")));
            wait.until(visibilityOfElementLocated(By.id("averagesReportCanvas")));

            System.out.println("TestReportPage SUCCESS");
        } catch (Throwable t) {
            takeScreenshot("TestReportPage_ERROR");
            throw t;
        }

        driver.quit();
    }

    @Test
    public void TestNickChangePage() {
        try {
            System.out.println("TestNickChangePage starts");
            initWebDriverToChrome();
            driver.get("http://localhost:3000/nickChange.html");
            final String pageTitle = driver.getTitle();
            assertEquals("promiseweb - Promise Card Game - change nick name", pageTitle, "FAIL: page title not equal!");

            longWait.until(elementToBeClickable(By.id("getGamesForNickChangeButton")));
            wait.until(elementToBeClickable(By.id("updateAllGameReportsButton")));

            // first without login credentials
            driver.findElement(By.id("getGamesForNickChangeButton")).click();
            wait.until(visibilityOfElementLocated(By.id("authAlertDiv")));
//            driver.findElement(By.className("close-alert-button")).click();

            // try with nonsense
            longWait.until(visibilityOfElementLocated(By.id("adminUser"))).clear();
            longWait.until(visibilityOfElementLocated(By.id("adminUser"))).sendKeys("juupaduupa");
            longWait.until(visibilityOfElementLocated(By.id("adminPass"))).clear();
            longWait.until(visibilityOfElementLocated(By.id("adminPass"))).sendKeys("juupaduupa");
            driver.findElement(By.id("getGamesForNickChangeButton")).click();
            wait.until(visibilityOfElementLocated(By.id("authAlertDiv")));
//            driver.findElement(By.className("close-alert-button")).click();

            // correct credentials
            longWait.until(visibilityOfElementLocated(By.id("adminUser"))).clear();
            longWait.until(visibilityOfElementLocated(By.id("adminUser"))).sendKeys(AdminCredentials.adminUser);
            longWait.until(visibilityOfElementLocated(By.id("adminPass"))).clear();
            longWait.until(visibilityOfElementLocated(By.id("adminPass"))).sendKeys(AdminCredentials.adminPass);
            driver.findElement(By.id("getGamesForNickChangeButton")).click();

            longWait.until(visibilityOfElementLocated(By.className("game-container-div")));
            List<WebElement> gameContainerDivs = driver.findElements(By.className("game-container-div"));
            assertTrue(gameContainerDivs.size() > 0, "FAIL: no game container divs!");

            boolean nickChangeTest = false;
            String nickChangeId = "";
            for (int i = 0; i < gameContainerDivs.size(); i++) {
                if (gameContainerDivs.get(i).findElement(By.className("report-players")).getText().contains("E2E-kaveri")) {
                    nickChangeTest = true;
                    nickChangeId = gameContainerDivs.get(i).findElement(By.className("report-players")).getAttribute("id");
                    gameContainerDivs.get(i).findElement(By.className("oldNameInput")).sendKeys("E2E-kaveri");
                    gameContainerDivs.get(i).findElement(By.className("newNameInput")).sendKeys("E2E-frendi");
                    System.out.println("Change nick in game: " + nickChangeId);
                    gameContainerDivs.get(i).findElement(By.className("change-nick-button")).click();
                    break;
                }
            }

            if (!nickChangeTest) {
                System.out.println("No E2E-kaveri -games, skipped changing nick");
            } else {
                String finalNickChangeId = nickChangeId;
                longWait.until(new ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        WebElement playersDiv = driver.findElement(By.id(finalNickChangeId));
                        if (playersDiv != null) {
                            if (playersDiv.getText().contains("E2E-frendi")) return true;
                        }
                        return false;
                    }
                });
            }

            System.out.println("TestNickChangePage SUCCESS");
        } catch (Throwable t) {
            takeScreenshot("TestNickChangePage_ERROR");
            throw t;
        }

        driver.quit();
    }

    @Test
    public void CreateAndLeaveGame() {
        try {
            System.out.println("CreateAndDeleteGame starts");
            initWebDriverToChrome();
            driver.get("http://localhost:3000");
            final String pageTitle = driver.getTitle();
            assertEquals("promiseweb - Promise Card Game", pageTitle, "FAIL: page title not equal!");

//            int gamesBefore = CountOpenGames();

            List<TestPlayer> testPlayers = getTestPlayers();
            CreateBaseGame(testPlayers.get(0));
//            int gamesAfter = CountOpenGames();
//            assertEquals(gamesBefore, gamesAfter-1, "FAIL: game count doesn't match!");
            LeaveBaseGame(testPlayers.get(0));
//            int gamesFinal = CountOpenGames();
//            assertEquals(gamesBefore, gamesFinal, "FAIL: game count doesn't match!");

            System.out.println("CreateAndDeleteGame SUCCESS");
        } catch (Throwable t) {
            takeScreenshot("CreateAndDeleteGame_ERROR");
            throw t;
        }

        driver.quit();
    }

    @Test
    public void PlayBaseGame() throws InterruptedException {
        try {
            System.out.println("PlayBaseGame starts");

            List<TestPlayer> testPlayers = getTestPlayers();
            Thread player1 = new Thread(new Player(testPlayers.get(0)), "player1Thread");
            Thread.sleep(5000);
            Thread player2 = new Thread(new Player(testPlayers.get(1)), "player2Thread");
            Thread.sleep(5000);
            Thread player3 = new Thread(new Player(testPlayers.get(2)), "player3Thread");
            Thread.sleep(5000);

            player1.start();
            player2.start();
            player3.start();

            System.out.println("PlayBaseGame SUCCESS");
        } catch (Exception t) {
            throw t;
        }

    }

    private int CountOpenGames() {
        // check if list is open
        if (driver.findElement(By.id("joinGameCollapse")).getText().contains("no open games")) {
            driver.findElement(By.id("openJoinGameDialogButton")).click();
            return 0;
        }
        List<WebElement> games = driver.findElements(By.className("gameContainerDiv"));
        if (games.size() > 0) {
            driver.findElement(By.id("openJoinGameDialogButton")).click();
            return games.size();
        }

        // if we are here then list is not open -> click button
        driver.findElement(By.id("openJoinGameDialogButton")).click();
        try {
            wait.until(visibilityOfElementLocated(By.className("gameContainerDiv")));
            games = driver.findElements(By.className("gameContainerDiv"));
            driver.findElement(By.id("openJoinGameDialogButton")).click();
            return games.size();
        } catch (Throwable t) {
            return 0;
        }
    }

    private void CreateBaseGame(TestPlayer creator) {
        try {
            driver.findElement(By.id("openCreateGameFormButton")).click();
            shortWait.until(visibilityOfElementLocated(By.id("newGameMyName"))).isEnabled();
            driver.findElement(By.id("newGameMyName")).sendKeys(creator.username);
            driver.findElement(By.id("password1")).sendKeys(creator.password);
            driver.findElement(By.id("password2")).sendKeys(creator.password);
            WebElement createButton = shortWait.until(presenceOfElementLocated(By.id("createNewGameButton")));
            Actions actions = new Actions(driver);
            actions.moveToElement(createButton);
            createButton.click();
        } catch (Throwable t) {
            throw t;
        }
    }

    private void LeaveBaseGame(TestPlayer testPlayer) {
        try {
            List<WebElement> games = driver.findElements(By.className("gameContainerDiv"));
            if (games.size() == 0) {
                // try if list is not open
                driver.findElement(By.id("openJoinGameDialogButton")).click();
                wait.until(presenceOfElementLocated(By.className("gameContainerRow")));
                games = driver.findElements(By.className("gameContainerRow"));
                assertTrue(games.size() > 0, "FAIL: No open games!");
                for (int i = 0; i < games.size(); i++) {
                    WebElement gameRow = games.get(i);
                    if (gameRow.findElement(By.className("leaveThisGameButton")).isEnabled()) {
                        gameRow.findElement(By.className("leaveThisGameButton")).click();
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            throw t;
        }
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

    private List<TestPlayer> getTestPlayers() {
        List<TestPlayer> testPlayers = new ArrayList<TestPlayer>();
        testPlayers.add(new TestPlayer("Testaaja", "demoTestaaja"));
        testPlayers.add(new TestPlayer("Demoilija", "demoTestaaja"));
        testPlayers.add(new TestPlayer("E2E-kaveri", "demoTestaaja"));
        return testPlayers;
    }
}
