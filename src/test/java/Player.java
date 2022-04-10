import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.Select;
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

    public Player(TestPlayer testPlayer, String gameUrl, int gameMode, int playerCount) {
        this.testPlayer = testPlayer;

        initWebDriverToChrome();

        try {
            driver.get(gameUrl);
            System.out.println("PLAYER "+this.testPlayer.username+" init");
            if (testPlayer.username.equals("Testaaja")) {
                // create game
                driver.findElement(By.id("openCreateGameFormButton")).click();
                shortWait.until(visibilityOfElementLocated(By.id("newGameMyName"))).isEnabled();
                if (playerCount > 3) {
                    Select playerSelect = new Select(driver.findElement(By.id("newGameHumanPlayersCount")));
                    playerSelect.selectByVisibleText(""+playerCount);
                }
                driver.findElement(By.id("newGameMyName")).sendKeys(testPlayer.username);
                driver.findElement(By.id("password1")).sendKeys(testPlayer.password);
                driver.findElement(By.id("password2")).sendKeys(testPlayer.password);
                if (gameMode != 0) {
                    System.out.println(gameMode);
                    Select startRound = new Select(driver.findElement(By.id("newGameStartRound")));
                    Select turnRound = new Select(driver.findElement(By.id("newGameTurnRound")));
                    Select endRound = new Select(driver.findElement(By.id("newGameEndRound")));
                    switch (gameMode) {
                        case 1:
                            // 6-5-6 -game
                            startRound.selectByVisibleText("6");
                            turnRound.selectByVisibleText("5");
                            endRound.selectByVisibleText("6");
                            break;
                    }
                }
                WebElement createButton = shortWait.until(presenceOfElementLocated(By.id("createNewGameButton")));
                Actions actions = new Actions(driver);
                actions.moveToElement(createButton);
                createButton.click();
                System.out.println("PLAYER "+this.testPlayer.username+" created game");
                takeScreenshot("CREATED_GAME_"+this.testPlayer.username);
            } else {
                // join game
                driver.findElement(By.id("openJoinGameDialogButton")).click();
                wait.until(visibilityOfElementLocated(By.className("gameContainerRow")));
                List<WebElement> games = driver.findElements(By.className("gameContainerRow"));
                System.out.println(games.size() + " available games for player "+this.testPlayer.username);
                for (int i = games.size() - 1; i >= 0; i--) {
                    WebElement game = games.get(i);
                    List<WebElement> listItems = game.findElements(By.className("player-in-game-item"));
                    if (listItems.size() > 0 && listItems.get(0).getText().equals("Testaaja")) {
                        // join in this game
                        game.findElement(By.className("newGameMyNameInput")).sendKeys(testPlayer.username);
                        game.findElement(By.className("newGameMyPass1")).sendKeys(testPlayer.password);
                        game.findElement(By.className("newGameMyPass2")).sendKeys(testPlayer.password);
                        game.findElement(By.className("joinThisGameButton")).click();
                        System.out.println("PLAYER "+this.testPlayer.username+" joined game");
                        takeScreenshot("JOINED_GAME_"+this.testPlayer.username);
                        return;
                    }
                }
                System.out.println("PLAYER "+this.testPlayer.username+" didn't found game to join!");
            }
        } catch (Throwable t) {
            takeScreenshot("INIT_ERROR_"+this.testPlayer.username);
            throw t;
        }
    }

    public Player(TestPlayer testPlayer, String gameUrl) {
        this(testPlayer, gameUrl, 0, 3);
    }

    public void run() {
        System.out.println("PLAYER "+this.testPlayer.username+" starts to play");
        takeScreenshot("GAME_ON_"+this.testPlayer.username);
        try {
            // get round count
            longWait.until(visibilityOfElementLocated(By.className("scoreboardTableRow")));
            final int rounds = driver.findElements(By.className("scoreboardTableRow")).size();
            for (int i = 0; i < rounds; i++) {
                // promise phase
                longWait.until(visibilityOfElementLocated(By.className("validPromiseButton")));
                takeScreenshot("ROUND_"+i+"_PROMISE_"+this.testPlayer.username);
                List<WebElement> promiseButtons = driver.findElements(By.className("validPromiseButton"));
                int rand = ThreadLocalRandom.current().nextInt(0, promiseButtons.size());
                final String promiseValue = promiseButtons.get(rand).getText();
                promiseButtons.get(rand).click();
                System.out.println("round "+i+", player "+this.testPlayer.username+" promised "+promiseValue);

                // play cards
                // first check how many cards are in this round
                longWait.until(visibilityOfElementLocated(By.cssSelector("#myCardsRowDiv.myCardsRowClass .cardCol .card")));
                final int cardsInRound = driver.findElements(By.cssSelector("#myCardsRowDiv.myCardsRowClass .cardCol .card")).size();
                System.out.println("round "+i+" has "+cardsInRound+" cards, player "+this.testPlayer.username);
                for (int j = 0; j < cardsInRound; j++) {
                    longWait.until(visibilityOfElementLocated(By.cssSelector("#myCardsRowDiv.myCardsRowClass .cardCol .activeCardInHand")));
                    longWait.until(visibilityOfElementLocated(By.cssSelector("#playerTable0.thinking-green-div")));
                    // takeScreenshot("ROUND_"+i+"_PLAY_CARD_"+(j+1)+"_"+this.testPlayer.username);
                    List<WebElement> playableCards = driver.findElements(By.cssSelector("#myCardsRowDiv.myCardsRowClass .cardCol .activeCardInHand"));
                    rand = ThreadLocalRandom.current().nextInt(0, playableCards.size());
                    final String playedCardClass = playableCards.get(rand).getAttribute("class");
                    System.out.println("round "+i+", card: "+(j+1)+", player "+this.testPlayer.username+" going to play "+playedCardClass);
                    playableCards.get(rand).click();
                    System.out.println("round "+i+", card: "+(j+1)+", player "+this.testPlayer.username+" played "+playedCardClass);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // game should be now played and stats graph visible
            longWait.until(visibilityOfElementLocated(By.id("oneGameReportModalLabel")));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Throwable r) {
            takeScreenshot("ERROR_"+this.testPlayer.username);
            throw r;
        } finally {
            takeScreenshot("GAME_OVER_"+this.testPlayer.username);
            System.out.println("FINALLY: "+this.testPlayer.username);
            if (driver != null) {
                System.out.println(" and quit driver: "+this.testPlayer.username);
                driver.quit();
            }
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
