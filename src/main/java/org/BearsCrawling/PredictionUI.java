package org.BearsCrawling;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.net.Socket;

    public class PredictionUI extends JFrame {
        private JButton strikeButton, ballButton, submitButton;
        private JTextArea logArea;
        private int strikeCount = 0;
        private int ballCount = 0;

        private PrintWriter out;
        private String userName;

        public PredictionUI(String userName) {
            this.userName = userName;

            setTitle("투구 예측 클라이언트 - " + userName);
            setSize(400, 300);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // 상단 패널: 버튼
            JPanel buttonPanel = new JPanel();
            strikeButton = new JButton("스트라이크");
            ballButton = new JButton("볼");
            submitButton = new JButton("제출");

            buttonPanel.add(strikeButton);
            buttonPanel.add(ballButton);
            buttonPanel.add(submitButton);

            // 로그 출력 영역
            logArea = new JTextArea();
            logArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(logArea);

            add(buttonPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);

            // 소켓 연결
            try {
                Socket socket = new Socket("localhost", 7777);
                out = new PrintWriter(socket.getOutputStream(), true);

                // 처음 접속 시 닉네임 전송
                out.println(userName);

                log("서버에 연결되었습니다.");
            } catch (Exception e) {
                log("서버 연결 실패: " + e.getMessage());
            }

            // 버튼 이벤트
            strikeButton.addActionListener(e -> {
                strikeCount++;
                log("스트라이크 추가 (" + strikeCount + ")");
            });

            ballButton.addActionListener(e -> {
                ballCount++;
                log("볼 추가 (" + ballCount + ")");
            });

            submitButton.addActionListener(e -> {
                sendPitchData();
                strikeCount = 0;
                ballCount = 0;
            });

            setVisible(true);
        }

        private void sendPitchData() {
            try {
                JSONObject json = new JSONObject();
                json.put("user", userName);
                json.put("type", "pitch");

                JSONObject data = new JSONObject();
                data.put("strike", strikeCount);
                data.put("ball", ballCount);

                json.put("data", data);

                out.println(json.toString());
                out.flush();

                log("제출 완료: 스트라이크=" + strikeCount + ", 볼=" + ballCount);
            } catch (Exception e) {
                log("전송 실패: " + e.getMessage());
            }
        }

        private void log(String message) {
            logArea.append(message + "\n");
        }

        public static void main(String[] args) {
            String name = JOptionPane.showInputDialog("닉네임을 입력하세요:");
            if (name != null && !name.isEmpty()) {
                new PredictionUI(name);
            }
        }
    }
