package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class crawlBaseBall {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            String url = "https://sports.daum.net/match/80090636?tab=cast";
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 페이지 로딩 대기
            Thread.sleep(8000);

            // 이닝 탭 리스트 수집 (data-tab 속성 있는 li 태그들)
            List<WebElement> inningTabs = driver.findElements(By.cssSelector(
                    ".list_tabsub2 li[data-tab^='inning']"
            ));

            for (WebElement tab : inningTabs) {
                WebElement link = tab.findElement(By.cssSelector("a.link_tab"));
                String inningName = link.getText().trim();
                if (inningName.isEmpty()) continue;

                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                Thread.sleep(2000); // 또는 WebDriverWait 사용

                System.out.println("===== " + inningName + " =====");


                List<WebElement> scoreItems = driver.findElements(By.cssSelector("span.item_element.type_score"));

                for (WebElement item : scoreItems) {
                    try {
                        WebElement ball = item.findElement(By.cssSelector("span.txt_ball"));
                        WebElement strike = item.findElement(By.cssSelector("span.txt_strike"));
                        System.out.print(ball.getText().trim() + " - ");
                        System.out.println(strike.getText().trim());
                    } catch (NoSuchElementException e) {
                        System.out.println("(txt_ball 없음)");
                    }
                }

                System.out.println();
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 드라이버 종료
            driver.quit();
        }
    }
}
