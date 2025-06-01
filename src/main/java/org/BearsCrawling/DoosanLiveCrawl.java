package org.BearsCrawling;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.util.ArrayList;


import java.time.Duration;
import java.util.List;

public class DoosanLiveCrawl {
    //요소가 나타날 때까지 기다림 presenceOfElementLocated
    //요소가 보일 때까지 기다림 visibilityOfElementLocated
    //클릭 가능할 때까지 기다림 elementToBeClickable
    //요소 사라질 때까지 기다림 invisibilityOfElementLocated
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        List<InningInfoDTO> inningInfoList = new ArrayList<>();

        try {
            // 이후 크롤링 로직 이어서 작성
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.get("https://sports.daum.net/baseball");
            WebElement doosanLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(text(), '두산')]")));

            if (doosanLink != null) {
                doosanLink.click();
                System.out.println("두산 팀 페이지로 이동했습니다.");
            }

            WebElement castTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='중계']")));
            castTab.click();

            // 이닝 탭 리스트 수집 (data-tab 속성 있는 li 태그들)
            List<WebElement> inningTabs = driver.findElements(By.cssSelector(
                    ".list_tabsub2 li[data-tab^='inning']"
            ));

            for (WebElement tab : inningTabs) {
                WebElement link = tab.findElement(By.cssSelector("a.link_tab"));
                String inningName = link.getText().trim();
                if (inningName.isEmpty()) continue;

                WebElement linkClickable = wait.until(ExpectedConditions.elementToBeClickable(link));
                linkClickable.click();
                Thread.sleep(2000);

                System.out.println("===== " + inningName + " =====");


                List<WebElement> histories = driver.findElements(By.cssSelector(".combo_history"));

                for (WebElement history : histories) {
                    WebElement player = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(history, By.cssSelector(".txt_player")));

                    String playerName = player.getText().trim();

                    int ballCount = 0;
                    int strikeCount = 0;

                    List<WebElement> scoreItems = history.findElements(By.cssSelector("span.item_element.type_score"));

                    for (WebElement item : scoreItems) {
                        try {
                            WebElement ball = item.findElement(By.cssSelector("span.txt_ball"));
                            WebElement strike = item.findElement(By.cssSelector("span.txt_strike"));

                            ballCount = Integer.parseInt(ball.getText().trim());
                            strikeCount = Integer.parseInt(strike.getText().trim());

                        } catch (NoSuchElementException e) {
                            // 무시
                        }
                    }
                    // 리스트에 추가
                    inningInfoList.add(new InningInfoDTO(inningName, playerName, strikeCount, ballCount));

                }
            }
            Gson gson = new Gson();
            try (FileWriter writer = new FileWriter("innings.json")) {
                gson.toJson(inningInfoList, writer);
                inningInfoList.toString();
                System.out.println("innings.json 파일로 저장 완료!");
            }

            //경기가 종료가 뜨면 크롤링 종료되도록 로직짜기

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            driver.quit();
        }

    }
}
