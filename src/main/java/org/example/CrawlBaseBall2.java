package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CrawlBaseBall2 {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        List<InningInfoDTOEX> inningInfoListEX2 = new ArrayList<>();

        try {
            String url = "https://sports.daum.net/match/80090636?tab=cast";
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 페이지 로딩 대기
            Thread.sleep(5000);

            // 이닝 탭 리스트 수집 (data-tab 속성 있는 li 태그들)
            List<WebElement> inningTabs = driver.findElements(By.cssSelector(
                    ".list_tabsub2 li[data-tab^='inning']"
            ));

            for (WebElement tab : inningTabs) {
                WebElement link = tab.findElement(By.cssSelector("a.link_tab"));
                String inningName = link.getText().trim();
                if (inningName.isEmpty()) continue;

                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                Thread.sleep(2000);

                System.out.println("===== " + inningName + " =====");


                List<WebElement> histories = driver.findElements(By.cssSelector(".combo_history"));

                for (WebElement history : histories) {
                    try {
                        WebElement player = history.findElement(By.cssSelector(".txt_player"));
                        String playerName = player.getText().trim();

                        System.out.println("타자 : " + player.getText().trim());
                        int ballCount = 0;
                        int strikeCount = 0;
                        List<WebElement> scoreItems = history.findElements(By.cssSelector("span.item_element.type_score"));

                        for (WebElement item : scoreItems) {
                            try {
                                WebElement ball = item.findElement(By.cssSelector("span.txt_ball"));
                                WebElement strike = item.findElement(By.cssSelector("span.txt_strike"));
                                ballCount = Integer.parseInt(ball.getText().trim());
                                strikeCount = Integer.parseInt(strike.getText().trim());
                                System.out.print(ball.getText().trim() + " - ");
                                System.out.println(strike.getText().trim());
                            } catch (NoSuchElementException e) {
                                System.out.println("(txt_ball 없음)");
                            }
                            // 리스트에 추가
                            inningInfoListEX2.add(new InningInfoDTOEX(inningName, playerName, strikeCount, ballCount));

                        }

                    } catch (NoSuchElementException e) {
                        System.out.println("(없음)");
                    }
                    System.out.println();
                }
                //JSON을 사람이 읽기 편한 포맷(줄바꿈, 들여쓰기)으로
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (FileWriter writer = new FileWriter("inningsEX2.json")) {
                    gson.toJson(inningInfoListEX2, writer);

                    System.out.println("inningsEX2.json 파일로 저장 완료!");
                }
            }
            } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 드라이버 종료
            driver.quit();
        }
    }
}
