package pt.amov.xicorafapaiva.sudoku.GameClasss;

import java.io.Serializable;
import java.util.ArrayList;

public class Cell implements Serializable {

    private int solution;
    private int player_value;
    private ArrayList<Integer> notes; // ????


    private int x;
    private int y;


    public int getSolution() {
        return solution;
    }

    public void setSolution(int solution) {
        this.solution = solution;
    }

    public int getPlayer_value() {
        return player_value;
    }

    public void setPlayer_value(int player_value) {
        this.player_value = player_value;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
