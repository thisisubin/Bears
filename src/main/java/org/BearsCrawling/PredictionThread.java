package org.BearsCrawling;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import org.json.JSONObject; // org.json 라이브러리 사용

public class PredictionThread implements Runnable {
    Socket child;
    BufferedReader ois;
    PrintWriter oos;

    String user_id;
    HashMap<String, PrintWriter> hm;
    InetAddress ip;
    String msg;

    public PredictionThread(Socket s, HashMap<String, PrintWriter> h) {
        child = s;
        hm = h;
        try {
            ois = new BufferedReader(new InputStreamReader
                    (child.getInputStream()));
            // Client로 받는거
            oos = new PrintWriter(child.getOutputStream());

            user_id = ois.readLine();

            ip = child.getInetAddress();


            System.out.println(ip + "로부터 " + user_id + "님이 접속하였습니다.");
            //출력 서버만 보는거고
            broadcast(user_id + "님이 접속하셨습니다."); // 일반 클라이언트

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

                    if (type.equals("pitch")) {
                        JSONObject pitchData = json.getJSONObject("data");
                        int strike = pitchData.getInt("strike");
                        int ball = pitchData.getInt("ball");

                        // 처리: 예를 들어 로그 찍기
                        System.out.println(user + "님 투구 결과: 스트라이크=" + strike + ", 볼=" + ball);

                        // 예: 전체 전파
                        broadcast(user + " 투구 결과: S=" + strike + ", B=" + ball);
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
