package org.BearsCrawling;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import org.json.JSONObject; // org.json 라이브러리 사용

public class ClientThread implements Runnable {
    Socket child;
    BufferedReader ois;
    PrintWriter oos;

    String user_id;
    HashMap<String, PrintWriter> hm;
    InetAddress ip;
    String msg;

    public ClientThread(Socket s, HashMap<String, PrintWriter> h) {
        child = s;
        hm = h;
        try {
            ois = new BufferedReader(new InputStreamReader
                    (child.getInputStream()));
            // Client로 받는거
            oos = new PrintWriter(child.getOutputStream(), true);

            user_id = ois.readLine();

            ip = child.getInetAddress();

            System.out.println(ip + "로부터 " + user_id + "님이 접속하였습니다."); //서버에게 보임
            broadcast(user_id); // 일반 클라이언트

            synchronized (hm) {
                hm.put(user_id, oos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        String receiveData;
        try {
            while ((receiveData = ois.readLine()) != null) {
                try {
                    JSONObject json = new JSONObject(receiveData);

                    String type = json.getString("type");
                    String user = json.getString("user");

                    //prompt : 크롤링한 이닝이름, 타자
                    if (type.equals("prompt")) {
                        String promptMsg = json.getString("data");
                        System.out.println("서버에서 받은 프롬프트: " + promptMsg);
                        broadcast("[정보] " + promptMsg);  // 꼭 [정보] 붙이기!

                    }
                    //pitch : 클라이언트의 예측 결과
                    if (type.equals("pitch")) {
                        JSONObject pitchData = json.getJSONObject("predData");
                        int strike = pitchData.getInt("strike");
                        int ball = pitchData.getInt("ball");

                        // 예측 저장
                        JSONObject predMsg = new JSONObject();
                        predMsg.put("strike", strike);
                        predMsg.put("ball", ball);
                        predMsg.put("user", user); // 닉네임도 함께

                        CrawlerThread.userPredictions.put(user, predMsg);

                        broadcast("[예측]" + predMsg); // 꼭 [정보] 붙이기!

                        // 처리: 예를 들어 로그 찍기
                        System.out.println(user + "님 투구 예측 결과: 스트라이크=" + strike + ", 볼=" + ball);
                    }

                    if (type.equals("result")) {
                        String resultMsg = json.getString("result");
                        System.out.println("서버에서 받은 result: " + resultMsg);
                        broadcast("[결과] " + resultMsg);  // 꼭 [결과] 붙이기!
                    }

                } catch (Exception e) {
                    System.out.println("JSON 파싱 실패: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void broadcast(String message) {
        synchronized (hm) {
            try {
                //향상된 포문
                // HashMap values 하면
                for (PrintWriter oos : hm.values()) {
                    oos.println(message);
                    oos.flush();

                }

            } catch (Exception e) {
            }
        }
    }
}
