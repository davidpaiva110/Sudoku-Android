package pt.amov.xicorafapaiva.sudoku.GameClasss;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import pt.isec.ans.sudokulibrary.Sudoku;

public class GameData extends ViewModel{

    public static final int BOARD_SIZE = 9;

    private int [][] board = null;
    private boolean [][] preSetNumbers = null;
    private int [][][] notes = null;

    public GameData() {
    }

    public int[][] getBoard() {
        return board;
    }

    public int getValue(int row, int column){
        return board[row][column];
    }

    public void setValue(int row, int column, int value){
        board[row][column] = value;
    }

    public void generateBoard(){
        String strJson = Sudoku.generate(10);
        try{
            JSONObject json = new JSONObject(strJson);
            if(json.optInt("result", 0) == 1){
                JSONArray jsonArray = json.getJSONArray("board");
                int [][] board = convert(jsonArray);
                this.board=board;
                 setPreSetNumbers();
                 initializeNotes();
            }
        } catch (Exception e){

        }
    }

    public void setBoard(ArrayList<Integer> tab, int nr, int nc) {
        board = new int[nr][nc];
        int aux = 0;
        for(int r = 0; r < nr; r++) {
            for (int c = 0; c < nc; c++) {
                //Log.i("PAIVAAAA", "nr: "+board.get(aux));
                board[r][c] = tab.get(aux++);
            }
        }
    }

    public  void setBoard(int [][] board){
        try {
            this.board = board;
            setPreSetNumbers();
            initializeNotes();
        }catch (Exception e){
            Log.i("PAIVAAAA", "excollooldlkgsldkglks");
        }

    }

    private int[][] convert(JSONArray jsonArray) {
        int [][] array = null;

        try {
            int rows = jsonArray.length(), columns = 0;
            for(int r = 0; r < rows; r++){
                JSONArray jsonRow = jsonArray.getJSONArray(r);
                if(r == 0){
                    columns = jsonRow.length();
                    array = new int[rows][columns];
                }
                for(int c = 0; c < columns; c++){
                    array[r][c] = jsonRow.getInt(c);
                }
            }
        } catch (Exception e){
            array=null;
        }

        return array;
    }

    private void setPreSetNumbers(){
        preSetNumbers = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if(board[r][c]!=0) preSetNumbers[r][c]=true;
                else preSetNumbers[r][c]=false;
            }
        }
    }

    public boolean isPreSet(int row, int column){
        return preSetNumbers[row][column];
    }

    private void initializeNotes(){
        notes = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                for (int k = 0; k < BOARD_SIZE; k++)
                    notes[i][j][k] = 0;
    }

    public int[] getCellNotes(int row, int column){
        return notes[row][column];
    }

    public int getCellNote(int row, int column, int position){
        return notes[row][column][position];
    }

    public void setCellNote(int row, int column, int position, int value){
        notes[row][column][position] = value;
    }

    public void resetCellNotes(int row, int column){
        for (int i = 0; i < BOARD_SIZE; i++) {
            notes[row][column][i] = 0;
        }
    }

}
