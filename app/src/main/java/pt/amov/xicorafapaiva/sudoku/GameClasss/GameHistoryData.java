package pt.amov.xicorafapaiva.sudoku.GameClasss;

import java.io.Serializable;

public class GameHistoryData implements Serializable {

    private String winer;
    private String gameMode;
    private int time;
    private int numbersAchive;

    public GameHistoryData(String winer, String gameMode, int time, int numbersAchive) {
        this.winer = winer;
        this.gameMode = gameMode;
        this.time = time;
        this.numbersAchive = numbersAchive;
    }

    public String getWiner() {
        return winer;
    }

    public String getGameMode() {
        return gameMode;
    }

    public int getTime() {
        return time;
    }

    public int getNumbersAchive() {
        return numbersAchive;
    }
}
