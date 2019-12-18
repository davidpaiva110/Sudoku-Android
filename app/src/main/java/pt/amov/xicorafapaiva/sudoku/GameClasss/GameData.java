package pt.amov.xicorafapaiva.sudoku.GameClasss;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

import pt.isec.ans.sudokulibrary.Sudoku;

public class GameData extends ViewModel implements Serializable {

    public static final int BOARD_SIZE = 9;

    private int [][] board = null;
    private int [][] solution = null;
    private boolean [][] preSetNumbers = null;
    private int [][][] notes = null;

    public GameData() {
        generateBoard();
        resolveBoard();
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

    public boolean numberIsValid(int row, int column){
        boolean isValid=true;
        for (int i = 0; i < BOARD_SIZE; i++) {
            if((board[row][i] == board[row][column] && i!=column))
                isValid = false;
            if((board[i][column] == board[row][column] && i!=row))
                isValid = false;
        }
        //return board[row][column]==solution[row][column];
        return isValid;
    }

    private void generateBoard(){
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

    private void resolveBoard() {
        try {
            JSONObject json = new JSONObject();
            JSONArray jsonArray = convert(board);
            json.put("board", jsonArray);
            String strJson = Sudoku.solve(json.toString(), 2000); //Se pusermos mais tempo para resolver tabuleiros mais difieis convem colocar numa thread
            try{
                json = new JSONObject(strJson);
                if(json.optInt("result", 0) == 1){
                    jsonArray = json.getJSONArray("board");
                    int [][] sol = convert(jsonArray);
                    solution = sol;
                }
            } catch (Exception e){

            }
        } catch (Exception e) {

        }
    }

    private JSONArray convert(int[][] board) {
        JSONArray jsonArray = new JSONArray();
        int rows = board.length, columns = 0;
        try {
            for (int r = 0; r < rows; r++){
                JSONArray jsonRow = new JSONArray();
                columns = board[r].length;
                for(int c = 0; c < columns; c++){
                    jsonRow.put(board[r][c]);
                }
                jsonArray.put(jsonRow);
            }
        } catch (Exception e) {

        }
        return jsonArray;
    }
}
