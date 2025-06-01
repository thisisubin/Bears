package org.BearsCrawling;

import com.google.gson.GsonBuilder;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


import java.time.Duration;
import java.util.List;

public class DoosanLiveCrawl {
    //요소가 나타날 때까지 기다림 presenceOfElementLocated
    //요소가 보일 때까지 기다림 visibilityOfElementLocated
    //클릭 가능할 때까지 기다림 elementToBeClickable
    //요소 사라질 때까지 기다림 invisibilityOfElementLocated
    public static void main(String[] args) throws IOException {
        // 크롤링 클래스 내부에서
        Socket socket = new Socket("localhost", 7777);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        List<InningInfoDTO> inningInfoList = new ArrayList<>();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.get("https://sports.daum.net/baseball");
            WebElement doosanLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(text(), '두산')]")));

            if (doosanLink != null) {
                doosanLink.click();
                System.out.println("두산 팀 페이지로 이동했습니다.");
            }

            WebElement castTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='중계']")));
            castTab.click();

            while(true) {
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
                    try {
                        WebElement player = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(history, By.cssSelector(".txt_player")));

                        String playerName = player.getText().trim();
                        System.out.println("타자 : " + player.getText().trim());

                       // promptMsg : 클라이언트로 보낼 예측대상 전송메세지 type : prompt
                        JSONObject promptMsg = new JSONObject();
                        promptMsg.put("user", "crawler");
                        promptMsg.put("type", "prompt");
                        promptMsg.put("data", inningName + " " + playerName + "의 투구 결과를 예측해주세요!");
                        out.println(promptMsg.toString());
                        out.flush();
                        System.out.println("전송한 메시지: " + promptMsg.toString());

                        Thread.sleep(3000); // 💡 클라이언트가 메시지 보고 반응할 수 있게 3초 대기
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
                                // 무시
                                System.out.println("(txt_ball 없음)");
                                System.out.println("(txt_strike 없음)");
                            }
                            // 리스트에 추가
                            inningInfoList.add(new InningInfoDTO(inningName, playerName, strikeCount, ballCount));
                        }
                    } catch (NoSuchElementException e) {
                        System.out.println("(없음)");
                    }
                    System.out.println();
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (FileWriter writer = new FileWriter("innings.json")) {
                    gson.toJson(inningInfoList, writer);
                    System.out.println("innings.json 파일로 저장 완료!");


                    //경기가 종료가 뜨면 크롤링 종료되도록 로직짜기
                }
                WebElement gameStatus = driver.findElement(By.cssSelector(".txt_g"));
                if (gameStatus.getText().contains("경기종료")) {
                    System.out.println("경기 종료");
                    break;
                }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            driver.quit();
        }
    }
}
