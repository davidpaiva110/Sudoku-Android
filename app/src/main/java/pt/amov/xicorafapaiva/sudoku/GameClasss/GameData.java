package pt.amov.xicorafapaiva.sudoku.GameClasss;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

import pt.isec.ans.sudokulibrary.Sudoku;

public class GameData extends ViewModel implements Serializable {

    public static final int BOARD_SIZE = 9;
    public static final int SUBGRID_SIZE = 3;

    private int [][] board = null;
    private int [][] invalidNumbers = null;
    private boolean [][] preSetNumbers = null;
    private int [][][] notes = null;
    private int [][][] invalideNotes = null;

    public GameData() {
        generateBoard();
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
        return invalidNumbers[row][column]==0;
    }

    public boolean noteIsValid(int row, int column, int position){
        return invalideNotes[row][column][position]==0;
    }

    public void validateNumber(int row, int column){
        validateNumber(row, column, 0);
    }

    public void validateNumber(int row, int column, int value){
        if (value != 0) board[row][column] = value;
        //Verifica se existe algum número igual na linha ou coluna
        for (int i = 0; i < BOARD_SIZE; i++) {
            if((board[row][i] == board[row][column] && i!=column)) {
                if(value == 0)
                    invalidNumbers[row][column] = board[row][column];
                else {
                    invalideNotes[row][column][value - 1] = value;
                    notes[row][column][value - 1] = 0;
                }
                board[row][column] = 0;
                return; //Como já é inválido não é necessário verificar as outras condições
            }
            if((board[i][column] == board[row][column] && i!=row)) {
                if(value == 0)
                    invalidNumbers[row][column] = board[row][column];
                else {
                    invalideNotes[row][column][value - 1] = value;
                    notes[row][column][value - 1] = 0;
                }
                board[row][column] = 0;
                return;
            }
        }
        //Verifica se existe algum número igual na subgrelha
        int subGridX=(column / SUBGRID_SIZE) * SUBGRID_SIZE; //É feita a divisão para colocar o elemento na primeira posição da subgrelha
        int subGridY=(row / SUBGRID_SIZE) * SUBGRID_SIZE; //É feita a divisão para colocar o elemento na primeira posição da subgrelha
        for (int r = 0; r < SUBGRID_SIZE; r++) {
            for (int c = 0; c < SUBGRID_SIZE; c++) {
                int subRow = subGridY + r;
                int subColumn = subGridX + c;
                if((subRow != row && subColumn != column) && board[subRow][subColumn] == board[row][column]){
                    if(value == 0)
                        invalidNumbers[row][column] = board[row][column];
                    else {
                        invalideNotes[row][column][value - 1] = value;
                        notes[row][column][value - 1] = 0;
                    }
                    board[row][column] = 0;
                    return;
                }
            }
        }
        //Verifica se ainda é possível a solução do puzzle
        if(resolveBoard() == null){
            if(value == 0)
                invalidNumbers[row][column] = board[row][column];
            else {
                invalideNotes[row][column][value - 1] = value;
                notes[row][column][value - 1] = 0;
            }
            board[row][column] = 0;
        }

        if (value != 0) board[row][column] = 0;
    }

    private void generateBoard(){
        String strJson = Sudoku.generate(25);
        try{
            JSONObject json = new JSONObject(strJson);
            if(json.optInt("result", 0) == 1){
                JSONArray jsonArray = json.getJSONArray("board");
                int [][] board = convert(jsonArray);
                this.board=board;
                 setPreSetNumbers();
                 initializeNotes();
                initializeInvalidNumbers();
            }
        } catch (Exception e){

        }
    }

    private void initializeInvalidNumbers() {
        invalidNumbers = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                invalidNumbers[i][j] = 0;
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
        invalideNotes = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                for (int k = 0; k < BOARD_SIZE; k++) {
                    notes[i][j][k] = 0;
                    invalideNotes[i][j][k] = 0;
                }
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

    private int[][] resolveBoard() {
        int [][] sol = null;
        try {
            JSONObject json = new JSONObject();
            JSONArray jsonArray = convert(board);
            json.put("board", jsonArray);
            String strJson = Sudoku.solve(json.toString(), 2000); //Se pusermos mais tempo para resolver tabuleiros mais difieis convem colocar numa thread
            try{
                json = new JSONObject(strJson);
                if(json.optInt("result", 0) == 1){
                    jsonArray = json.getJSONArray("board");
                    sol = convert(jsonArray);
                }
            } catch (Exception e){

            }
        } catch (Exception e) {

        }
        return sol;
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

    public void resetInvalidNumber(int row, int column) {
        invalidNumbers[row][column] = 0;
    }

    public void resetInvalidNote(int row, int column, int position) {
        invalideNotes[row][column][position] = 0;
    }

    public int getInvalidNumber(int row, int column) {
        return invalidNumbers[row][column];
    }

    public int getInvalidNote(int row, int column, int position){
        return invalideNotes[row][column][position];
    }

    public void validateNotesAfterNewValidNumber(int row, int column) {
        //Valida as notas da linha e coluna
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) { //Percorre todas as notas
                if(i!=column && notes[row][i][j] != 0 && board[row][i] == 0)
                    validateNumber(row, i, j + 1);
                if(i!=row && notes[i][column][j] != 0 && board[i][column] == 0)
                    validateNumber(i, column, j + 1);
            }
        }
        //Valida as notas da subgrelha
        int subGridX=(column / SUBGRID_SIZE) * SUBGRID_SIZE; //É feita a divisão para colocar o elemento na primeira posição da subgrelha
        int subGridY=(row / SUBGRID_SIZE) * SUBGRID_SIZE; //É feita a divisão para colocar o elemento na primeira posição da subgrelha
        for (int r = 0; r < SUBGRID_SIZE; r++) {
            for (int c = 0; c < SUBGRID_SIZE; c++) {
                int subRow = subGridY + r;
                int subColumn = subGridX + c;
                for (int j = 0; j < BOARD_SIZE; j++) //Percorre todas as notas
                    if(subRow != row && subColumn != column && notes[subRow][subColumn][j] != 0 && board[subRow][subColumn] == 0)
                    validateNumber(subRow, subColumn, j + 1);
            }
        }
    }
}
