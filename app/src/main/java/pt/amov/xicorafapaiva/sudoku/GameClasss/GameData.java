package pt.amov.xicorafapaiva.sudoku.GameClasss;


import android.util.Log;

import androidx.lifecycle.ViewModel;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.Serializable;

import pt.amov.xicorafapaiva.sudoku.GameViews.PlayerProfileActivity;
import pt.isec.ans.sudokulibrary.Sudoku;

public class GameData extends ViewModel implements Serializable {

    public static final int BOARD_SIZE = 9;
    public static final int SUBGRID_SIZE = 3;
    public static final int INITIAL_PLAYER_TIME = 30;
    public static final int CORRECT_NUMBER_TIME = 20;
    public static final int MAX_PLAYERS = 3;

    private int [][] board = null;
    private int [][] invalidNumbers = null;
    private boolean [][] preSetNumbers = null;
    private int [][][] notes = null;
    private int [][][] invalideNotes = null;
    private int gameTime = 0;
    private boolean finished = false;
    private int gameMode;
    private int [] playerScores = null;
    private int [][] numberInsertedPlayer = null; // Jogador que inseriu um número
    // Estruturas para modos 2 e 3
    private int [][][] notesPlayer2 = null;
    private int playerTime = INITIAL_PLAYER_TIME; // Tempo que um jogador tem numa jogada
    private int player = 1; // Indica qual o jogador que está a jogar

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

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public int getPlayerOfInsertedNumber(int row, int column){
        return numberInsertedPlayer[row][column];
    }

    public void setPlayerOfInsertedNumber(int row, int column){
        numberInsertedPlayer[row][column] = player;
    }

    public int getPlayer() {
        return player;
    }

    public void nextPlayer() {
        if(player == 1) {
            player = 2;
        } else {
            player = 1;
        }
        playerTime = INITIAL_PLAYER_TIME;
        resetInvalidNotes(); //Reset para não aparecerem as jogadas inválidas do outro jogador na mudança de jogador
    }

    public void decrementPlayerTime(){
        this.playerTime--;
    }

    public int getPlayerTime() {
        return playerTime;
    }

    public boolean numberIsValid(int row, int column){
        return invalidNumbers[row][column]==0;
    }

    public boolean noteIsValid(int row, int column, int position){
        return invalideNotes[row][column][position]==0;
    }

    public void validateNumber(int row, int column){
        validateNumber(row, column, 0, 0);
    }

    public void validateNumber(int row, int column, int value, int playerOfNotes){
        if (value != 0){
            board[row][column] = value;
        }
        //Verifica se existe algum número igual na linha ou coluna
        for (int i = 0; i < BOARD_SIZE; i++) {
            if((board[row][i] == board[row][column] && i!=column)) {
                if(value == 0)
                    invalidNumbers[row][column] = board[row][column];
                else {
                    if(playerOfNotes == player)
                        invalideNotes[row][column][value - 1] = value;
                    if(playerOfNotes == 1)
                        notes[row][column][value - 1] = 0;
                    if(playerOfNotes == 2)
                        notesPlayer2[row][column][value - 1] = 0;
                }
                board[row][column] = 0;
                numberInsertedPlayer[row][column] = 0;
                return; //Como já é inválido não é necessário verificar as outras condições
            }
            if((board[i][column] == board[row][column] && i!=row)) {
                if(value == 0)
                    invalidNumbers[row][column] = board[row][column];
                else {
                    if(playerOfNotes == player)
                        invalideNotes[row][column][value - 1] = value;
                    if(playerOfNotes == 1)
                        notes[row][column][value - 1] = 0;
                    if(playerOfNotes == 2)
                        notesPlayer2[row][column][value - 1] = 0;
                }
                board[row][column] = 0;
                numberInsertedPlayer[row][column] = 0;
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
                        if(playerOfNotes == player)
                            invalideNotes[row][column][value - 1] = value;
                        if(playerOfNotes == 1)
                            notes[row][column][value - 1] = 0;
                        if(playerOfNotes == 2)
                            notesPlayer2[row][column][value - 1] = 0;
                    }
                    board[row][column] = 0;
                    numberInsertedPlayer[row][column] = 0;
                    return;
                }
            }
        }
        //Verifica se ainda é possível a solução do puzzle
        if(resolveBoard() == null){
            if(value == 0)
                invalidNumbers[row][column] = board[row][column];
            else {
                if(playerOfNotes == player)
                    invalideNotes[row][column][value - 1] = value;
                if(playerOfNotes == 1)
                    notes[row][column][value - 1] = 0;
                if(playerOfNotes == 2)
                    notesPlayer2[row][column][value - 1] = 0;
            }
            board[row][column] = 0;
            numberInsertedPlayer[row][column] = 0;
        }

        if (value != 0) board[row][column] = 0;
    }


    private void initializeInvalidNumbers() {
        invalidNumbers = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                invalidNumbers[i][j] = 0;
    }


    public  void setBoard(int [][] board){
        try {
            this.board = board;
            setPreSetNumbers();
            initializeNotes();
            initializeInvalidNumbers();
            initializeNumberInsertedPlayer();
            initializePlayerScores();
        }catch (Exception e){
        }
    }

    private void initializeNumberInsertedPlayer() {
        numberInsertedPlayer = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                numberInsertedPlayer[i][j] = 0;
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
        notesPlayer2 = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        invalideNotes = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                for (int k = 0; k < BOARD_SIZE; k++) {
                    notes[i][j][k] = 0;
                    notesPlayer2[i][j][k] = 0;
                    invalideNotes[i][j][k] = 0;
                }
    }

    public int[] getCellNotes(int row, int column){
        return notes[row][column];
    }

    public int[] getPlayer2CellNotes(int row, int column){
        return notesPlayer2[row][column];
    }

    public int getCellNote(int row, int column, int position){
        return notes[row][column][position];
    }

    public int getPlayer2CellNote(int row, int column, int position){
        return notesPlayer2[row][column][position];
    }

    public void setCellNote(int row, int column, int position, int value){
        notes[row][column][position] = value;
    }

    public void setPlayer2CellNote(int row, int column, int position, int value){
        notesPlayer2[row][column][position] = value;
    }

    public void resetCellNotes(int row, int column){
        for (int i = 0; i < BOARD_SIZE; i++) {
            notes[row][column][i] = 0;
        }
    }

    public void resetPlayer2CellNotes(int row, int column){
        for (int i = 0; i < BOARD_SIZE; i++) {
            notesPlayer2[row][column][i] = 0;
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

    public void resetInvalidNotes(){
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                for (int k = 0; k < BOARD_SIZE; k++) {
                    resetInvalidNote(i, j, k);
                }
            }
        }
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
                if (i != column && notes[row][i][j] != 0 && board[row][i] == 0)
                    validateNumber(row, i, j + 1, 1);
                if (i != row && notes[i][column][j] != 0 && board[i][column] == 0)
                    validateNumber(i, column, j + 1, 1);
                if(gameMode == 1){
                    if (i != column && notesPlayer2[row][i][j] != 0 && board[row][i] == 0)
                        validateNumber(row, i, j + 1, 2);
                    if (i != row && notesPlayer2[i][column][j] != 0 && board[i][column] == 0)
                        validateNumber(i, column, j + 1, 2);
                }
            }
        }
        //Valida as notas da subgrelha
        int subGridX=(column / SUBGRID_SIZE) * SUBGRID_SIZE; //É feita a divisão para colocar o elemento na primeira posição da subgrelha
        int subGridY=(row / SUBGRID_SIZE) * SUBGRID_SIZE; //É feita a divisão para colocar o elemento na primeira posição da subgrelha
        for (int r = 0; r < SUBGRID_SIZE; r++) {
            for (int c = 0; c < SUBGRID_SIZE; c++) {
                int subRow = subGridY + r;
                int subColumn = subGridX + c;
                for (int j = 0; j < BOARD_SIZE; j++) { //Percorre todas as notas
                    if (subRow != row && subColumn != column && notes[subRow][subColumn][j] != 0 && board[subRow][subColumn] == 0)
                        validateNumber(subRow, subColumn, j + 1, 1);
                    if (gameMode == 1) {
                        if (subRow != row && subColumn != column && notesPlayer2[subRow][subColumn][j] != 0 && board[subRow][subColumn] == 0)
                            validateNumber(subRow, subColumn, j + 1, 2);
                    }
                }
            }
        }
    }

    public void incrementGameTime(){
        this.gameTime++;
    }

    public int getGameTime() {
        return gameTime;
    }

    public boolean isFinished() {
        return finished;
    }

    public void checkTerminateGame(){
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(board[i][j] == 0)
                    return; // Ainda existem espaços em branco e o jogo ainda não acabou
            }
        }
        // O jogo terminou
        finished = true;
    }

    public void showSolution(){
        board = resolveBoard();
        finished = true;
    }

    public void setCorrectNumberTime(){
        playerTime = CORRECT_NUMBER_TIME;
    }

    public void updatePlayerScore() {
        playerScores[player - 1]++;
    }

    private void initializePlayerScores(){
        playerScores = new int [MAX_PLAYERS];
        for (int i = 0; i < MAX_PLAYERS; i++) {
            playerScores[i] = 0;
        }
    }

    public int getPlayerScore(int player){
        return playerScores[player - 1];
    }

    public void setPlayer(int player) {
        this.player = player;
    }
}
