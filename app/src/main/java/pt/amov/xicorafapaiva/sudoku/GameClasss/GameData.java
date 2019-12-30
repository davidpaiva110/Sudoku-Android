package pt.amov.xicorafapaiva.sudoku.GameClasss;


import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.lifecycle.ViewModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;

import pt.amov.xicorafapaiva.sudoku.GameViews.PlayerProfileActivity;
import pt.isec.ans.sudokulibrary.Sudoku;

public class GameData extends ViewModel implements Serializable {

    public static final int BOARD_SIZE = 9;
    public static final int SUBGRID_SIZE = 3;
    public static final int INITIAL_PLAYER_TIME = 30;
    public static final int CORRECT_NUMBER_TIME = 20;
    public static final int MAX_PLAYERS = 3;
    public static final int MAX_CLIENTS = 2;

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
    private int [][][] notesPlayer3 = null;
    private int playerTime = INITIAL_PLAYER_TIME; // Tempo que um jogador tem numa jogada
    private int player = 1; // Indica qual o jogador que está a jogar

    private Socket[] gameSockets = null;
    private BufferedReader[] gameInputs;
    private PrintWriter[] gameOutputs;
    private boolean isServidor = false; //Indica se é servidor ou cliente

    //Nomes dos Jogadores
    private ArrayList<String> playerNames;

    public GameData() {
        playerNames = new ArrayList<String>();
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

    public boolean isServidor() {
        return isServidor;
    }

    public void setServidor(boolean servidor) {
        isServidor = servidor;
    }

    public void setGameSocket(int pos, Socket gameSocket) {
        this.gameSockets[pos] = gameSocket;
    }

    public void setGameInput(int pos, BufferedReader gameInput) {
        this.gameInputs[pos] = gameInput;
    }

    public void setGameOutput(int pos, PrintWriter gameOutput) {
        this.gameOutputs[pos] = gameOutput;
    }

    public void setGameSockets(Socket[] gameSockets) {
        this.gameSockets = gameSockets;
    }

    public Socket[] getGameSockets() {
        return gameSockets;
    }

    public Socket getGameSocket(int pos) {
        return gameSockets[pos];
    }

    public BufferedReader getGameInput(int pos) {
        return gameInputs[pos];
    }

    public PrintWriter getGameOutputs(int pos) {
        return gameOutputs[pos];
    }

    public ArrayList<String> getPlayerNames() {
        return playerNames;
    }

    public void initializeCommunicationVariables(){
        gameSockets = new Socket[MAX_CLIENTS];
        gameInputs = new BufferedReader[MAX_CLIENTS];
        gameOutputs = new PrintWriter[MAX_CLIENTS];
        for (int i = 0; i < MAX_CLIENTS; i++) {
            gameSockets[i] = null;
            gameInputs[i] = null;
            gameOutputs[i] = null;
        }
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
        if(gameMode == 1) {
            if (player == 1) {
                player = 2;
            } else {
                player = 1;
            }
        } else if(gameMode == 2){
            if (player == 1) {
                player = 2;
            } else if(player == 2){
                if(playerNames.size() > 2)
                    player = 3;
                else
                    player = 1;

            } else if(player == 3)
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
                    if(playerOfNotes == 3)
                        notesPlayer3[row][column][value - 1] = 0;
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
                    if(playerOfNotes == 3)
                        notesPlayer3[row][column][value - 1] = 0;
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
                        if(playerOfNotes == 3)
                            notesPlayer3[row][column][value - 1] = 0;
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
                if(playerOfNotes == 3)
                    notesPlayer3[row][column][value - 1] = 0;
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

    public void setBoardFromOtherMode(int [][] board){
        this.board = board;
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
        notesPlayer3 = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        invalideNotes = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                for (int k = 0; k < BOARD_SIZE; k++) {
                    notes[i][j][k] = 0;
                    notesPlayer2[i][j][k] = 0;
                    notesPlayer3[i][j][k] = 0;
                    invalideNotes[i][j][k] = 0;
                }
    }

    public int[] getCellNotes(int row, int column){
        return notes[row][column];
    }

    public int[] getPlayer2CellNotes(int row, int column){
        return notesPlayer2[row][column];
    }

    public int[] getPlayer3CellNotes(int row, int column){
        return notesPlayer3[row][column];
    }

    public int getCellNote(int row, int column, int position){
        return notes[row][column][position];
    }

    public int getPlayer2CellNote(int row, int column, int position){
        return notesPlayer2[row][column][position];
    }

    public int getPlayer3CellNote(int row, int column, int position){
        return notesPlayer3[row][column][position];
    }

    public void setCellNote(int row, int column, int position, int value){
        notes[row][column][position] = value;
    }

    public void setPlayer2CellNote(int row, int column, int position, int value){
        notesPlayer2[row][column][position] = value;
    }

    public void setPlayer3CellNote(int row, int column, int position, int value){
        notesPlayer3[row][column][position] = value;
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

    public void resetPlayer3CellNotes(int row, int column){
        for (int i = 0; i < BOARD_SIZE; i++) {
            notesPlayer3[row][column][i] = 0;
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
                if(gameMode > 1){
                    if (i != column && notesPlayer2[row][i][j] != 0 && board[row][i] == 0)
                        validateNumber(row, i, j + 1, 2);
                    if (i != row && notesPlayer2[i][column][j] != 0 && board[i][column] == 0)
                        validateNumber(i, column, j + 1, 2);
                    if(playerNames.size() > 2){
                        if (i != column && notesPlayer3[row][i][j] != 0 && board[row][i] == 0)
                            validateNumber(row, i, j + 1, 3);
                        if (i != row && notesPlayer3[i][column][j] != 0 && board[i][column] == 0)
                            validateNumber(i, column, j + 1, 3);
                    }
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
                    if (gameMode > 1) {
                        if (subRow != row && subColumn != column && notesPlayer2[subRow][subColumn][j] != 0 && board[subRow][subColumn] == 0)
                            validateNumber(subRow, subColumn, j + 1, 2);
                        if(playerNames.size() > 2){
                            if (subRow != row && subColumn != column && notesPlayer3[subRow][subColumn][j] != 0 && board[subRow][subColumn] == 0)
                                validateNumber(subRow, subColumn, j + 1, 3);
                        }
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

    public int getPlayerWinner(){
        if(isFinished()){
            if(playerScores[0] > playerScores[1] && playerScores[0] > playerScores[2]) return 0;
            if(playerScores[1] > playerScores[0] && playerScores[1] > playerScores[2]) return 1;
            if(playerScores[2] > playerScores[1] && playerScores[2] > playerScores[0]) return 2;
        }
        return -1;
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
        //Notificação dos clientes caso seja o modo 3
        if(gameMode == 2){
            Thread thNotify = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < MAX_CLIENTS; i++) {
                        if(gameSockets[i] != null){
                            try {
                                boolean isWinner = false;
                                //Verificação se é o vencedor ou não
                                if(getPlayerWinner() == i + 1)
                                    isWinner = true;
                                //Enviar a indicação que o jogo terminou aos clientes
                                JSONObject jsonFinish = new JSONObject();
                                jsonFinish.put("finish", true);
                                jsonFinish.put("winner", isWinner);
                                jsonFinish.put("winnerName", getPlayerName(getPlayerWinner()));
                                jsonFinish.put("time", getGameTime());
                                jsonFinish.put("numbersAchieved", getPlayerScore(getPlayerWinner() + 1));
                                gameOutputs[i].println(jsonFinish.toString());
                                gameOutputs[i].flush();
                            } catch (JSONException e) {
                            }
                        }
                    }
                }
            });
            thNotify.start();
        }
    }

    public void showSolution(){
        board = resolveBoard();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(numberInsertedPlayer[i][j] == 0)
                    numberInsertedPlayer[i][j] = 1;
            }
        }
        finished = true;
    }

    public void setCorrectNumberTime(){
        playerTime = CORRECT_NUMBER_TIME;
    }

    public void incrementPlayerScore() {
        playerScores[player - 1]++;
    }

    public void decrementPlayerScore() {
        playerScores[player - 1]--;
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


    public void addPlayerName(String playerName){
        if(playerNames != null){
            if (playerNames.size() != MAX_PLAYERS){
                playerNames.add(playerName);
            }
        }
    }

    public String getPlayerName(int indexPLayer){
        if(indexPLayer > playerNames.size())
            return null;
        return playerNames.get(indexPLayer);
    }

    public int[][] getInvalidNumbers() {
        return invalidNumbers;
    }

    public void setInvalidNumbers(int[][] invalidNumbers) {
        this.invalidNumbers = invalidNumbers;
    }

    public boolean[][] getPreSetNumbers() {
        return preSetNumbers;
    }

    public void setPreSetNumbers(boolean[][] preSetNumbers) {
        this.preSetNumbers = preSetNumbers;
    }

    public int[][][] getNotes() {
        return notes;
    }

    public void setNotes(int[][][] notes) {
        this.notes = notes;
    }

    public int[][][] getInvalideNotes() {
        return invalideNotes;
    }

    public void setInvalideNotes(int[][][] invalideNotes) {
        this.invalideNotes = invalideNotes;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int[] getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(int[] playerScores) {
        this.playerScores = playerScores;
    }

    public int[][] getNumberInsertedPlayer() {
        return numberInsertedPlayer;
    }

    public void setNumberInsertedPlayer(int[][] numberInsertedPlayer) {
        this.numberInsertedPlayer = numberInsertedPlayer;
    }

    public String toStringJSONFormat(){
        JSONObject gameDatajson = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            json.put("finished", finished);
            json.put("gameMode", gameMode);
            json.put("playerTime", playerTime);
            json.put("player", player);
            JSONArray board = new JSONArray();
            JSONArray invalidNumbers = new JSONArray();
            JSONArray preSetNumbers = new JSONArray();
            JSONArray numberInsertedPlayer = new JSONArray();
            JSONArray notes = new JSONArray();
            JSONArray invalideNotes = new JSONArray();
            JSONArray notesPlayer2 = new JSONArray();
            JSONArray playerScores = new JSONArray();
            JSONArray playerNames = new JSONArray();
            JSONArray notesPlayer3 = new JSONArray();
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    board.put(this.board[i][j]);
                    invalidNumbers.put(this.invalidNumbers[i][j]);
                    preSetNumbers.put(this.preSetNumbers[i][j]);
                    numberInsertedPlayer.put(this.numberInsertedPlayer[i][j]);
                    for (int k = 0; k < BOARD_SIZE; k++) {
                        notes.put(this.notes[i][j][k]);
                        invalideNotes.put(this.invalideNotes[i][j][k]);
                        notesPlayer2.put(this.notesPlayer2[i][j][k]);
                        notesPlayer3.put(this.notesPlayer3[i][j][k]);
                    }
                }
            }
            for (int i = 0; i < MAX_PLAYERS; i++) {
                playerScores.put(this.playerScores[i]);
            }
            for (String name:this.playerNames) {
                playerNames.put(name);
            }
            json.put("board", board);
            json.put("invalidNumbers", invalidNumbers);
            json.put("preSetNumbers", preSetNumbers);
            json.put("numberInsertedPlayer", numberInsertedPlayer);
            json.put("notes", notes);
            json.put("invalideNotes", invalideNotes);
            json.put("notesPlayer2", notesPlayer2);
            json.put("notesPlayer3", notesPlayer3);
            json.put("playerScores", playerScores);
            json.put("playerNames", playerNames);
            json.put("gameTime", gameTime);
        } catch (JSONException e) {
        }
        return json.toString();
    }

    public void updateThroughJSON(JSONObject json){
        int intObj, intObj2;
        boolean boleanObj;
        try {
            intObj = (int) json.get("gameTime");
            gameTime = intObj;
            boleanObj = (boolean) json.get("finished");
            finished = boleanObj;
            intObj = (int) json.get("gameMode");
            gameMode = intObj;
            intObj = (int) json.get("playerTime");
            playerTime = intObj;
            intObj = (int) json.get("player");
            player = intObj;
            JSONArray board = json.optJSONArray("board");
            JSONArray invalidNumbers = json.optJSONArray("invalidNumbers");
            JSONArray preSetNumbers = json.optJSONArray("preSetNumbers");
            JSONArray numberInsertedPlayer = json.optJSONArray("numberInsertedPlayer");
            JSONArray notes = json.optJSONArray("notes");
            JSONArray invalideNotes = json.optJSONArray("invalideNotes");
            JSONArray notesPlayer2 = json.optJSONArray("notesPlayer2");
            JSONArray notesPlayer3 = json.optJSONArray("notesPlayer3");
            JSONArray playerScores = json.optJSONArray("playerScores");
            JSONArray playerNames = json.optJSONArray("playerNames");
            if(this.board == null) this.board = new int[BOARD_SIZE][BOARD_SIZE];
            if(this.invalidNumbers == null) this.invalidNumbers = new int[BOARD_SIZE][BOARD_SIZE];
            if(this.preSetNumbers == null) this.preSetNumbers = new boolean[BOARD_SIZE][BOARD_SIZE];
            if(this.numberInsertedPlayer == null) this.numberInsertedPlayer = new int[BOARD_SIZE][BOARD_SIZE];
            if(this.notes == null) this.notes = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
            if(this.notesPlayer2 == null) this.notesPlayer2 = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
            if(this.notesPlayer3 == null) this.notesPlayer3 = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
            if(this.invalideNotes == null) this.invalideNotes = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
            if(this.playerScores == null) this.playerScores = new int[MAX_PLAYERS];
            if(this.playerNames == null) this.playerNames = new ArrayList<>();
            intObj = intObj2 = 0;
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    this.board[i][j] = board.getInt(intObj);
                    this.invalidNumbers[i][j] = invalidNumbers.optInt(intObj);
                    this.preSetNumbers[i][j] = preSetNumbers.optBoolean(intObj);
                    this.numberInsertedPlayer[i][j] = numberInsertedPlayer.optInt(intObj);
                    for (int k = 0; k < BOARD_SIZE; k++) {
                        this.notes[i][j][k] = notes.optInt(intObj2);
                        this.invalideNotes[i][j][k] = invalideNotes.optInt(intObj2);
                        this.notesPlayer2[i][j][k] = notesPlayer2.optInt(intObj2);
                        this.notesPlayer3[i][j][k] = notesPlayer3.optInt(intObj2);
                        intObj2++;
                    }
                    intObj++;
                }
            }
            for (int i = 0; i < playerScores.length(); i++) {
                this.playerScores[i] = playerScores.optInt(i);
            }
            this.playerNames.clear();
            for (int i = 0; i < playerNames.length(); i++) {
                this.playerNames.add(playerNames.optString(i));
            }
        } catch (JSONException e) {
        }
    }

    public void sendMoveToServer(int row, int column, int value, boolean onNotas, boolean onApagar){
        if(!isServidor){
            try {

                JSONObject jsonMove = new JSONObject();
                jsonMove.put("row", row);
                jsonMove.put("column", column);
                jsonMove.put("onNotas", onNotas);
                jsonMove.put("onApagar", onApagar);
                jsonMove.put("value", value);
                gameOutputs[0].println(jsonMove.toString());
                gameOutputs[0].flush();
            } catch (JSONException e) {
            }
        }
    }
}
