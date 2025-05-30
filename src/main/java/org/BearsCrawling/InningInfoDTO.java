package org.BearsCrawling;

public class InningInfoDTO {
    String inningName;
    String player;
    int strike;
    int ball;
    public InningInfoDTO(String inningName, String player, int strike, int ball) {
        this.inningName = inningName;
        this.player = player;
        this.strike = strike;
        this.ball = ball;
    }
    public String getInningName() {
        return inningName;
    }

    public void setInningName(String inningName) {
        this.inningName = inningName;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getStrike() {
        return strike;
    }

    public void setStrike(int strike) {
        this.strike = strike;
    }

    public int getBall() {
        return ball;
    }

    public void setBall(int ball) {
        this.ball = ball;
    }

    @Override
    public String toString() {
        return "InningInfo{" +
                "inningName='" + inningName + '\'' +
                ", player='" + player + '\'' +
                ", strike=" + strike +
                ", ball=" + ball +
                '}';
    }
}
