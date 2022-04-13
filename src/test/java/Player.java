import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class Player implements Runnable {
    public WebDriver driver;

    public WebDriverWait shortWait;
    public WebDriverWait wait;
    public WebDriverWait longWait;
    public WebDriverWait observeWait;

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
            } else if (!this.testPlayer.observer) {
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
            } else if (this.testPlayer.observer) {
                observeRequest();
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
            if (this.testPlayer.observer) {
                // just take some screenshots
                for (int i = 0; i < rounds; i++) {
                    try {
                        System.out.println("starting to observe round "+i+" by " + this.testPlayer.username);
                        observeWait.until(invisibilityOfElementLocated(By.cssSelector("#player0Points"+i+".avgHistory")));
                        System.out.println("round "+i+" is observed");
                        takeScreenshot("ROUND_"+i+"_OBSERVED_"+this.testPlayer.username);
                    } catch (UnhandledAlertException a) {
                        System.out.println("round "+i+" and alert box is visible " + this.testPlayer.username);
                        Alert alert = driver.switchTo().alert();
                        final String alertText = alert.getText();
                        assertTrue(alertText.contains("You have now left observing the game"));
                        alert.accept();
                        takeScreenshot("ROUND_"+i+"_OBSERVED_ALERT_"+this.testPlayer.username);

                        // join again:
                        driver.navigate().refresh();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        observeRequest();
                        longWait.until(visibilityOfElementLocated(By.className("scoreboardTableRow")));
                        observeWait.until(invisibilityOfElementLocated(By.cssSelector("#player0Points"+i+".avgHistory")));
                        System.out.println("starting to observe again round "+i+" by " + this.testPlayer.username);
                        System.out.println("round "+i+" is observed");
                        takeScreenshot("ROUND_"+i+"_OBSERVED_"+this.testPlayer.username);
                    }
                }
            } else {
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
                        if (j > 0) {
                            try {
                                System.out.println("check if there is observer waiting and accept if exists round "+i+" and card "+(j+1)+" "+this.testPlayer.username);
                                final WebElement obsButton = shortWait.until(visibilityOfElementLocated(By.cssSelector("#openObserversButton.btn-warning")));
                                obsButton.click();
                                wait.until(visibilityOfElementLocated(By.id("observersModal")));
                                final WebElement allowBtn = shortWait.until(visibilityOfElementLocated(By.cssSelector(".obs-allow-btn")));
                                takeScreenshot("ROUND_"+i+"_CARD_"+(j+1)+"_ALLOW_OBSERVE_"+this.testPlayer.username);
                                allowBtn.click();
                                System.out.println("observer accepted round "+i+" and card "+(j+1)+" "+this.testPlayer.username);
                                wait.until(visibilityOfElementLocated(By.id("closeObserveModalBtn"))).click();
                            } catch (Throwable t) {
                                System.out.println("no OBS button enabled round "+i+" and card "+(j+1)+" "+this.testPlayer.username);
                                // do nothing
                            }
                        }
                        if (i == 1 && j == 0) {
                            try {
                                System.out.println("check if there is observer and deny if exists round "+i+" and card "+(j+1)+" "+this.testPlayer.username);
                                final WebElement obsButton = shortWait.until(visibilityOfElementLocated(By.cssSelector("#openObserversButton.btn-success")));
                                obsButton.click();
                                wait.until(visibilityOfElementLocated(By.id("observersModal")));
                                final WebElement denyBtn = shortWait.until(visibilityOfElementLocated(By.cssSelector(".obs-deny-btn")));
                                takeScreenshot("ROUND_"+i+"_CARD_"+(j+1)+"_DENIED_OBSERVE_"+this.testPlayer.username);
                                denyBtn.click();
                                System.out.println("observer denied round "+i+" and card "+(j+1)+" "+this.testPlayer.username);
                                wait.until(visibilityOfElementLocated(By.id("closeObserveModalBtn"))).click();
                            } catch (Throwable t) {
                                System.out.println("checking of observer and denying fails round "+i+" and card "+(j+1)+" "+this.testPlayer.username);
                                // do nothing
                            }
                        }
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
        observeWait = new WebDriverWait(driver, Duration.ofSeconds(240));
    }

    private void initWebDriverToChrome() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1900x1200");
        options.addArguments("--incognito");
        options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
        // options.setHeadless(true);
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

    private void observeRequest() {
        try {
            driver.findElement(By.id("openOngoingGamesDialogButton")).click();
            wait.until(visibilityOfElementLocated(By.id("gamesContainerDiv")));
            List<WebElement> onGoingGames = driver.findElements(By.className("onGoingGameRowStatus1"));
            assertTrue(onGoingGames.size() > 0, "FAIL: no ongoing games");
            for (int i = onGoingGames.size()-1; i >= 0; i--) {
                String playersStr = onGoingGames.get(i).findElement(By.className("report-players")).getText();
                if (playersStr.contains("Testaaja") && playersStr.contains("Aku Ankka") && playersStr.contains("KOM-puutteri")) {
                    Actions actions = new Actions(driver);

                    // first without username and password
                    final WebElement obsButton = onGoingGames.get(i).findElement(By.className("observeGameButton"));
                    actions.moveToElement(obsButton);
                    //obsButton.click();
                    //final String errorTxt = wait.until(visibilityOfElementLocated(By.id("authOngoingGamesAlertDiv"))).getText();
                    //assertTrue(errorTxt.contains("Authentication error"), "FAIL: no auth error");

                    shortWait.until(visibilityOfElementLocated(By.id("observerName"))).clear();
                    shortWait.until(visibilityOfElementLocated(By.id("observerName"))).sendKeys(this.testPlayer.username);
                    shortWait.until(visibilityOfElementLocated(By.id("observerPass"))).clear();
                    shortWait.until(visibilityOfElementLocated(By.id("observerPass"))).sendKeys(this.testPlayer.password);
                    actions.moveToElement(obsButton);
                    obsButton.click();
                    final String waitingText = wait.until(visibilityOfElementLocated(By.id("waitingGameAlertDiv"))).getText();
                    assertTrue(waitingText.contains("Waiting players to allow"), "FAIL: no waiting text");
                    System.out.println("starting to wait observing allowance " + this.testPlayer.username);
                    takeScreenshot("OBSERVE_REQUESTED_"+this.testPlayer.username);
                    return;
                }
            }
        } catch (Throwable t) {
            throw t;
        }
    }
}
