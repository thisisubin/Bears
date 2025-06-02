package org.BearsCrawling;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private JButton strikeButton, ballButton, submitButton;
    private JTextArea logArea;
    private JLabel statusLabel;

    private int strikeCount = 0;
    private int ballCount = 0;

    private PrintWriter out;
    private BufferedReader in;
    private String userName;

    public Client(String userName) {
        this.userName = userName;

        setTitle("투구 예측 클라이언트 - " + userName);
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        // 하단 패널: 버튼
        JPanel buttonPanel = new JPanel();
        strikeButton = new JButton("🔴 STRIKE 🔴");
        ballButton = new JButton("🟢 BALL 🟢");
        submitButton = new JButton("SUBMIT");

        buttonPanel.add(strikeButton);
        buttonPanel.add(ballButton);
        buttonPanel.add(submitButton);

        // 로그 출력 영역
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        add(statusLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);

        // 소켓 연결 및 수신 쓰레드 시작
        try {
            System.out.println("*****클라이언트*****");//출력
            Socket socket = new Socket("ip주소", 7777);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 처음 접속 시 닉네임 전송
            out.println(userName);
            log("서버에 연결되었습니다.");
            // 수신 쓰레드 시작
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        if (msg.startsWith("[정보]")) {
                            updateStatus(msg.replace("[정보]", "").trim());
                        } else if (msg.startsWith("[예측]")){
                            log(msg);
                        } else if (msg.startsWith("[결과]")) {
                            log(msg);
                        }

                    }
                } catch (IOException e) {
                    log("서버로부터 수신 실패: " + e.getMessage());
                }
            }).start();


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

    //서버로 보낼 클라이언트의 예측 데이터
    private void sendPitchData() {
        try {
            JSONObject json = new JSONObject();
            json.put("user", userName);
            json.put("type", "pitch");

            JSONObject predData = new JSONObject();
            predData.put("strike", strikeCount);
            predData.put("ball", ballCount);

            json.put("predData", predData);

            out.println(json.toString());
            out.flush();

            log("제출 완료: 스트라이크=" + strikeCount + ", 볼=" + ballCount);
        } catch (Exception e) {
            log("전송 실패: " + e.getMessage());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
    private String lastMessage = "";

    private void updateStatus(String status) {
        if (!status.equals(lastMessage)) {
            lastMessage = status;
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("📢 " + status);
                log("[상태 변경] " + status);
            });
        } else {
            log("[중복 상태] " + status);
        }
    }

    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog("닉네임을 입력하세요:");
        if (name != null && !name.isEmpty()) {
            new Client(name);
        }else {
            System.out.println("닉네임을 입력하지 않아 프로그램을 종료합니다.");
        }
    }
}
