import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class LandingTest {
    public WebDriver driver;

    public WebDriverWait shortWait;
    public WebDriverWait wait;
    public WebDriverWait longWait;

    private String adminUser = "";
    private String adminPass = "";

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
        } finally {
            if (driver != null) driver.quit();
        }
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
        } finally {
            if (driver != null) driver.quit();
        }
    }

    @Test
    public void TestNickChangePage() throws IOException {
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
            readAdminKeys();
            longWait.until(visibilityOfElementLocated(By.id("adminUser"))).clear();
            longWait.until(visibilityOfElementLocated(By.id("adminUser"))).sendKeys(this.adminUser);
            longWait.until(visibilityOfElementLocated(By.id("adminPass"))).clear();
            longWait.until(visibilityOfElementLocated(By.id("adminPass"))).sendKeys(this.adminPass);
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
        } catch (IOException i) {
            System.out.println("ERROR reading admin credentials");
            throw i;
        } catch (Throwable t) {
            takeScreenshot("TestNickChangePage_ERROR");
            throw t;
        } finally {
            if (driver != null) driver.quit();
        }
    }

    @Test
    public void CreateAndLeaveGame() {
        try {
            System.out.println("CreateAndLeaveGame starts");
            initWebDriverToChrome();
            driver.get("http://localhost:3000");
            final String pageTitle = driver.getTitle();
            assertEquals("promiseweb - Promise Card Game", pageTitle, "FAIL: page title not equal!");

            List<TestPlayer> testPlayers = getTestPlayers();
            CreateBaseGame(testPlayers.get(0));
            LeaveBaseGame(testPlayers.get(0));

            System.out.println("CreateAndLeaveGame SUCCESS");
        } catch (Throwable t) {
            takeScreenshot("CreateAndLeaveGame_ERROR");
            throw t;
        } finally {
            if (driver != null) driver.quit();
        }
    }

    @Test
    public void PlayBaseGame() throws InterruptedException {
        try {
            System.out.println("PlayBaseGame starts");
            final List<TestPlayer> testPlayers = getTestPlayers();
            final int initSleepTime = 3000;
            final boolean[] weHaveError = {false};
            final String[] errorText = new String[1];

            final Thread player1 = new Thread(new Player(testPlayers.get(0), 1, testPlayers.size()), "player1Thread");
            Thread.sleep(5000);
            final Thread player2 = new Thread(new Player(testPlayers.get(1)), "player2Thread");
            Thread.sleep(5000);
            final Thread player3 = new Thread(new Player(testPlayers.get(2)), "player3Thread");
            Thread.sleep(5000);
            final Thread player4 = new Thread(new Player(testPlayers.get(3)), "player4Thread");
            Thread.sleep(5000);
            final Thread player5 = new Thread(new Player(testPlayers.get(4)), "player5Thread");

            Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread th, Throwable ex) {
                    System.out.println("Uncaught exception: " + ex);
                    weHaveError[0] = true;
                    errorText[0] = ex.toString();
                }
            };

            player1.setUncaughtExceptionHandler(h);
            player2.setUncaughtExceptionHandler(h);
            player3.setUncaughtExceptionHandler(h);
            player4.setUncaughtExceptionHandler(h);
            player5.setUncaughtExceptionHandler(h);

            Thread.sleep(5000);

            player1.start();
            Thread.sleep(initSleepTime);
            player2.start();
            Thread.sleep(initSleepTime);
            player3.start();
            Thread.sleep(initSleepTime);
            player4.start();
            Thread.sleep(initSleepTime);
            player5.start();
            Thread.sleep(initSleepTime);

            final int sleepTime = 5000;
            int rounds = 1;
            boolean gameIsOn = player1.isAlive() && player2.isAlive() && player3.isAlive() && player4.isAlive() && player5.isAlive();
            while (gameIsOn) {
                Thread.sleep(sleepTime);
                System.out.println("Game has been on "+rounds*sleepTime/1000+" seconds");
                gameIsOn = player1.isAlive() && player2.isAlive() && player3.isAlive() && player4.isAlive() && player5.isAlive();
                if (!gameIsOn) {
                    System.out.println("Game is OFF");
                    // wait few seconds so all threads will be ready
                    Thread.sleep(5000);
                }
                rounds++;
            }
            while (player1.isAlive() || player2.isAlive() || player3.isAlive() || player4.isAlive() || player5.isAlive()) {
                if (player1.isAlive()) System.out.println(" player1 still alive...");
                if (player2.isAlive()) System.out.println(" player2 still alive...");
                if (player3.isAlive()) System.out.println(" player3 still alive...");
                if (player4.isAlive()) System.out.println(" player4 still alive...");
                if (player5.isAlive()) System.out.println(" player5 still alive...");
                System.out.println("Waiting all threads to end...");
                Thread.sleep(5000);
            }
            if (weHaveError[0]) {
                throw new InterruptedException("ERRORIA PUKKAA: "+ errorText[0]);
            }

            System.out.println("PlayBaseGame SUCCESS");
        } catch (Exception r) {
            throw r;
        } catch (Throwable t) {
            throw t;
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
                try {
                    driver.findElement(By.id("openJoinGameDialogButton")).click();
                    wait.until(presenceOfElementLocated(By.className("gameContainerRow")));
                } catch (Exception e) {
                    driver.findElement(By.id("openJoinGameDialogButton")).click();
                    wait.until(presenceOfElementLocated(By.className("gameContainerRow")));
                }
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
        testPlayers.add(new TestPlayer("KOM-puutteri", "demoTestaaja"));
        testPlayers.add(new TestPlayer("Aku Ankka", "demoTestaaja"));
        return testPlayers;
    }

    private void readAdminKeys() throws IOException {
        InputStream inputStream;
        try {
            Properties prop = new Properties();
            final String propFileName = "admin.properties";
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
                this.adminUser = prop.getProperty("ADMINUSER");
                this.adminPass = prop.getProperty("ADMINPASS");
            } else {
                throw new FileNotFoundException("admin secrets file "+propFileName+" not found!");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            throw e;
        }
    }
}
