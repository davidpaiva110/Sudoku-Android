package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.GameClasss.GameHistoryData;
import pt.amov.xicorafapaiva.sudoku.GameClasss.GameHistoryViewModel;
import pt.amov.xicorafapaiva.sudoku.R;

public class GameBoardActivity extends AppCompatActivity {

    public static final int SECOND = 1000;
    private static final int PORT = 8899;
    public static final int TIMEOUT = 2000;

    private Board sudokuView;
    private Drawable btBackground;

    // ViewModel dos dados do Jogo
    private GameData gameData;

    //Estruturas para o modo 3
    private ProgressDialog pd;
    private boolean isProgressDialogActive = false;
    private Handler procMsg = new Handler();
    private ServerSocket serverSocket=null;
    Thread serverCommunicationPlayer1 = null, serverCommunicationPlayer2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int mode = getIntent().getIntExtra("mode", 0);
        if(mode == 0)
            setContentView(R.layout.activity_game_board);
        else if(mode == 1)
            setContentView(R.layout.activity_game_board_m2);
        else if(mode == 2)
            setContentView(R.layout.activity_game_board_m3);
        this.gameData = ViewModelProviders.of(this).get(GameData.class);
        if(savedInstanceState == null) {
            if(getIntent().getBooleanExtra("existingGame", false) == true){  //Modo 2/3 -> Modo 1
                GameData auxGameData = (GameData) getIntent().getSerializableExtra("gameData");
                gameData.setInvalidNumbers(auxGameData.getInvalidNumbers());
                gameData.setPreSetNumbers(auxGameData.getPreSetNumbers());
                gameData.setNotes(auxGameData.getNotes());
                gameData.setInvalideNotes(auxGameData.getInvalideNotes());
                gameData.setGameTime(auxGameData.getGameTime());
                gameData.setFinished(auxGameData.isFinished());
                gameData.setGameMode(auxGameData.getGameMode());
                gameData.setPlayerScores(auxGameData.getPlayerScores());
                gameData.setNumberInsertedPlayer(auxGameData.getNumberInsertedPlayer());
                gameData.setBoard(auxGameData.getBoard());
            } else {
                gameData.setServidor(getIntent().getBooleanExtra("isServidor", false));
                if(!(mode == 2 && !gameData.isServidor())) {
                    //Player1 Name
                    gameData.addPlayerName(PlayerProfileActivity.getPlayerName(this));
                    //Player2 Name
                    if (mode == 1)
                        gameData.addPlayerName(getIntent().getStringExtra("player2Name"));
                    int nr = getIntent().getIntExtra("nr", 9);
                    int nc = getIntent().getIntExtra("nc", 9);
                    ArrayList<Integer> alBoard = getIntent().getIntegerArrayListExtra("board");
                    int[][] tabuleiro = new int[nr][nc];
                    int aux = 0;
                    for (int r = 0; r < nr; r++) {
                        for (int c = 0; c < nc; c++) {
                            tabuleiro[r][c] = alBoard.get(aux);
                            aux++;
                        }
                    }
                    this.gameData.setBoard(tabuleiro);
                }
                this.gameData.setGameMode(mode);
            }
            FrameLayout flSudoku = findViewById(R.id.flSudoku);
            sudokuView = new Board(this, this.gameData);
            flSudoku.addView(sudokuView);
            btBackground = findViewById(R.id.btnNotas).getBackground();
            initializeButtons();
            if(mode == 2)
                gameData.initializeCommunicationVariables();
            if(gameData.getGameMode() != 2)
                thTempo.start();
            if(!(mode == 2 && !gameData.isServidor()))
                initializaPlayerNames();
            if(mode == 2){
                if(gameData.isServidor()) {
                    createProgressDialogServer();
                    isProgressDialogActive = true;
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                serverSocket = new ServerSocket(PORT);
                                for (int i = 0; i < GameData.MAX_CLIENTS; i++) {
                                    gameData.setGameSocket(i, serverSocket.accept());
                                    //Criação dos inputs e outputs
                                    gameData.setGameInput(i, new BufferedReader(new InputStreamReader(gameData.getGameSocket(i).getInputStream())));
                                    gameData.setGameOutput(i, new PrintWriter(gameData.getGameSocket(i).getOutputStream()));
                                    //Recebimento do nome do jogador
                                    String nameJSON = gameData.getGameInput(i).readLine();
                                    JSONObject jsonObject = new JSONObject(nameJSON);
                                    gameData.addPlayerName(jsonObject.getString("name"));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            initializaPlayerNames();
                                            Toast.makeText(getApplicationContext(), R.string.strNovoClienteLigado, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                serverSocket.close();
                                serverSocket = null;
                            } catch (SocketException ex){

                            }catch (Exception e) {
                                gameData.setGameSockets(null);
                            }
                            procMsg.post(new Runnable() {
                                @Override
                                public void run() {
                                    pd.dismiss();
                                    isProgressDialogActive = false;
                                    if (gameData.getGameSockets() == null) {
                                        Toast.makeText(getApplicationContext(), R.string.strErroComunicacao, Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                    serverCommunicationPlayer1 = new Thread(new RunnableThreadServer(2));
                                    serverCommunicationPlayer1.start();
                                    if(gameData.getGameSocket(1) != null) {
                                        serverCommunicationPlayer2 = new Thread(new RunnableThreadServer(3));
                                        serverCommunicationPlayer2.start();
                                    }
                                    thTempo.start();
                                }
                            });
                        }
                    });
                    t.start();
                }
                else{  //Cliente Modo 3
                    createProgressDialogClient();
                    isProgressDialogActive = true;
                    String serverIP = getIntent().getStringExtra("serverIP");
                    int serverPORT = getIntent().getIntExtra("serverPORT", 8899);
                    startCliente(serverIP, serverPORT);
                }
            }
        }
    }

    public void startCliente(final String serverIP, final int serverPORT){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gameData.setGameSocket(0, new Socket());
                    gameData.getGameSocket(0).connect(new InetSocketAddress(serverIP, serverPORT), TIMEOUT);

                } catch (Exception e) {
                    gameData.setGameSocket(0, null);
                }
                if (gameData.getGameSocket(0) == null) {
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.str_erro_ligaçãoCliente) + serverIP, Toast.LENGTH_LONG).show();
                            pd.dismiss();
                            finish();
                        }
                    });
                    return;
                }
                clientCommunication.start();
            }
        });
        t.start();
    }

    Thread thTempo = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Colocar logo inicialmente os segundos e as cores nas textViews
                        TextView tvTempoJogo = findViewById(R.id.tvTempoJogo);
                        if(gameData.getGameMode() == 0)
                            tvTempoJogo.setText("" + gameData.getGameTime());
                        else if(gameData.getGameMode() > 0) {
                            tvTempoJogo.setText("" + gameData.getPlayerTime());
                            updatePlayersColors();
                        }
                    }
                });
                while(!gameData.isFinished()) {
                    Thread.sleep(SECOND);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tvTempoJogo = findViewById(R.id.tvTempoJogo);
                            gameData.incrementGameTime();
                            if(gameData.getGameMode() == 0)
                                tvTempoJogo.setText("" + gameData.getGameTime());
                            else if(gameData.getGameMode() > 0){
                                gameData.decrementPlayerTime();
                                if(gameData.getPlayer() == 1)
                                    ((TextView)findViewById(R.id.tvPontosJogador1)).setText("" + gameData.getPlayerScore(1));
                                else if(gameData.getPlayer() == 2)
                                    ((TextView)findViewById(R.id.tvPontosJogador2)).setText("" + gameData.getPlayerScore(2));
                                if(gameData.getGameMode() == 2 && gameData.getPlayer() == 3)
                                    ((TextView)findViewById(R.id.tvPontosJogador3)).setText("" + gameData.getPlayerScore(3));
                                if(gameData.getPlayerTime() < 0){
                                    gameData.nextPlayer();
                                    updatePlayersColors();
                                    sudokuView.postInvalidate();
                                }
                                tvTempoJogo.setText("" + gameData.getPlayerTime());
                            }
                        }
                    });
                    if(gameData.getGameMode() == 2){
                        sendGameDataToClients();
                    }
                }

            } catch (InterruptedException e) {
            }
        }
    });

    private void updatePlayersColors(){
        //Atualiza as cores dos nomes do jogador para destacar o jogador atual
        if(gameData.getPlayer() == 1){
            ((TextView)findViewById(R.id.tvNomePlayer1)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer1));
            ((TextView)findViewById(R.id.tvPontosJogador1)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer1));
            ((TextView)findViewById(R.id.tvStrPontosJogador1)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer1));
            ((TextView)findViewById(R.id.tvNomePlayer2)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvPontosJogador2)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvStrPontosJogador2)).setTextColor(getResources().getColor(R.color.colorGray));
            if(gameData.getGameMode() == 2) {
                ((TextView) findViewById(R.id.tvNomePlayer3)).setTextColor(getResources().getColor(R.color.colorGray));
                ((TextView) findViewById(R.id.tvPontosJogador3)).setTextColor(getResources().getColor(R.color.colorGray));
                ((TextView) findViewById(R.id.tvStrPontosJogador3)).setTextColor(getResources().getColor(R.color.colorGray));
            }
        }
        else if(gameData.getPlayer() == 2){
            ((TextView)findViewById(R.id.tvNomePlayer2)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer2));
            ((TextView)findViewById(R.id.tvPontosJogador2)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer2));
            ((TextView)findViewById(R.id.tvStrPontosJogador2)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer2));
            ((TextView)findViewById(R.id.tvNomePlayer1)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvPontosJogador1)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvStrPontosJogador1)).setTextColor(getResources().getColor(R.color.colorGray));
            if(gameData.getGameMode() == 2) {
                ((TextView) findViewById(R.id.tvNomePlayer3)).setTextColor(getResources().getColor(R.color.colorGray));
                ((TextView) findViewById(R.id.tvPontosJogador3)).setTextColor(getResources().getColor(R.color.colorGray));
                ((TextView) findViewById(R.id.tvStrPontosJogador3)).setTextColor(getResources().getColor(R.color.colorGray));
            }
        }
        else if(gameData.getPlayer() == 3 && gameData.getGameMode() == 2){
            ((TextView)findViewById(R.id.tvNomePlayer3)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer3));
            ((TextView)findViewById(R.id.tvPontosJogador3)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer3));
            ((TextView)findViewById(R.id.tvStrPontosJogador3)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer3));
            ((TextView)findViewById(R.id.tvNomePlayer1)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvPontosJogador1)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvStrPontosJogador1)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvNomePlayer2)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvPontosJogador2)).setTextColor(getResources().getColor(R.color.colorGray));
            ((TextView)findViewById(R.id.tvStrPontosJogador2)).setTextColor(getResources().getColor(R.color.colorGray));
        }
    }

    // Criação do Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        int mode = getIntent().getIntExtra("mode", 1);
        if(mode == 0)
            inflater.inflate(R.menu.menu_game_board_activity, menu);
        else if(mode > 0)
            inflater.inflate(R.menu.menu_modo_2_e_3, menu);

        return true;
    }

    // Processamento das opções selecionadas no menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.homeIcon:
                DialogConfirmBackHome dialog = new DialogConfirmBackHome();
                dialog.show(getSupportFragmentManager(),"idConfirmarDialog");
                return true;
            case R.id.solutionIcon:
                if(!gameData.isFinished()) {
                    DialogConfirmShowSolution dialogSol = new DialogConfirmShowSolution(sudokuView);
                    dialogSol.show(getSupportFragmentManager(), "idSolutionDialog");
                }
                return true;
            case R.id.m1ButtonMenu:   // Botão de volta ao modo 1
                Intent myIntent;
                myIntent = new Intent(getBaseContext(),   GameBoardActivity.class);
                myIntent.putExtra("gameData", gameData);
                myIntent.putExtra("mode", 0);
                myIntent.putExtra("existingGame", true);
                DialogConfirmChangeToM1 dialogChange = new DialogConfirmChangeToM1(gameData, myIntent);
                dialogChange.show(getSupportFragmentManager(), "idChangeDialog");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void initializeButtons(){
        Button btn = (Button)findViewById(R.id.btnNumber1);
        sudokuView.setSelectedValue(1);
        resetNumbersColor();
        btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btn.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    // onClick dos Números
    public void onNumberPress(View view) {
        Button btn = (Button) view;
        String stringNumber = btn.getText().toString();
        int number = Integer.parseInt(stringNumber);
        sudokuView.setSelectedValue(number);
        resetNumbersColor();
        btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btn.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    private void resetNumbersColor(){
        int [] buttonsIDs = {R.id.btnNumber1,
                R.id.btnNumber2,
                R.id.btnNumber3,
                R.id.btnNumber4,
                R.id.btnNumber5,
                R.id.btnNumber6,
                R.id.btnNumber7,
                R.id.btnNumber8,
                R.id.btnNumber9};
        for (int i = 0; i < 9; i++) {
            findViewById(buttonsIDs[i]).setBackground(btBackground);
            ((Button)findViewById(buttonsIDs[i])).setTextColor(getResources().getColor(R.color.colorBlack));
        }
    }

    public void onBtnNotas(View view) {
        if(sudokuView.isOnNotas()) {
            sudokuView.setOnNotas(false);
            view.setBackground(btBackground);
            ((Button)view).setTextColor(getResources().getColor(R.color.colorBlack));
        }else{
            sudokuView.setOnNotas(true);
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ((Button)view).setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    public void onBtnApagar(View view) {
        if(sudokuView.isOnApagar()) {
            sudokuView.setOnApagar(false);
            view.setBackground(btBackground);
            ((Button)view).setTextColor(getResources().getColor(R.color.colorBlack));
        }else{
            sudokuView.setOnApagar(true);
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ((Button)view).setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        this.gameData = sudokuView.getGameData();
        outState.putBoolean("onApagar", sudokuView.getOnApagar());
        outState.putBoolean("pd", isProgressDialogActive);
        outState.putBoolean("onNotas", sudokuView.getOnNotas());
        outState.putInt("selectedValue", sudokuView.getSelectedValue());
        thTempo.interrupt();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int selectedValue = savedInstanceState.getInt("selectedValue");
        isProgressDialogActive = savedInstanceState.getBoolean("pd");
        boolean isOnNotas = savedInstanceState.getBoolean("onNotas");
        boolean isOnApagar = savedInstanceState.getBoolean("onApagar");
        sudokuView = new Board(this, this.gameData, selectedValue,
                isOnNotas,
                isOnApagar);
        FrameLayout flSudoku = findViewById(R.id.flSudoku);
        flSudoku.addView(sudokuView);
        btBackground = findViewById(R.id.btnNotas).getBackground();
        restoreButtonsSettings(selectedValue, isOnNotas, isOnApagar);
        initializaPlayerNames();
        if(!(gameData.getGameMode() == 2 && !gameData.isServidor()))
            thTempo.start();
        if(isProgressDialogActive == true) {
            if(gameData.isServidor())
                createProgressDialogServer();
            else
                createProgressDialogClient();
        }
    }

    public void createProgressDialogServer(){
        String ip = getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setTitle(getString(R.string.strEsperarClientes));
        pd.setMessage(getString(R.string.strIniciarJogo) + "\n(IP: " + ip + ")");
        pd.setCancelable(false);
        pd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.strCancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(serverSocket!=null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                    }
                }
                dialog.dismiss();
                finish();
            }
        });
        pd.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.strIniciar), (DialogInterface.OnClickListener) null);
        pd.show();

        //Este listener é feito depois do show() para ser possível carregar no botão sem fechar automaticamente a Dialog
        Button pdButton = pd.getButton(DialogInterface.BUTTON_POSITIVE);
        pdButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick ( View view ) {
                if(gameData.getGameSocket(0) != null) { //Se houver pelo menos 1 cliente
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                    }
                } else{
                    Toast.makeText(getApplicationContext(), R.string.strSemJogadoresLigados, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void createProgressDialogClient(){
        pd = new ProgressDialog(this);
        pd.setTitle(getString(R.string.strEsperarInicio));
        pd.setMessage(getString(R.string.strEsperarInicioServidor));
        pd.setCancelable(false);
        pd.show();
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void restoreButtonsSettings(int selectedButton, boolean isOnNotas, boolean isOnApagar){
        int [] buttonsIDs = {R.id.btnNumber1,
                R.id.btnNumber2,
                R.id.btnNumber3,
                R.id.btnNumber4,
                R.id.btnNumber5,
                R.id.btnNumber6,
                R.id.btnNumber7,
                R.id.btnNumber8,
                R.id.btnNumber9};

        selectedButton--;
        resetNumbersColor();
        for (int i = 0; i < 9; i++) {
            if(i == selectedButton){
                findViewById(buttonsIDs[i]).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                ((Button)findViewById(buttonsIDs[i])).setTextColor(getResources().getColor(R.color.colorWhite));
            }
        }
        if(isOnNotas){
            Button btnNotas = (Button)findViewById(R.id.btnNotas);
            btnNotas.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            btnNotas.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(isOnApagar){
            Button btnApagar = (Button)findViewById(R.id.btnApagar);
            btnApagar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            btnApagar.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    public void initializaPlayerNames(){
        if(gameData.getGameMode() == 1){  // Modo 2
            TextView tvName1 = findViewById(R.id.tvNomePlayer1);
            tvName1.setText(gameData.getPlayerName(0));
            TextView tvName2 = findViewById(R.id.tvNomePlayer2);
            tvName2.setText(gameData.getPlayerName(1));
            ImageView imageView = findViewById(R.id.ivPlayer1);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f=new File(directory.getAbsolutePath(), "profile.jpg");
            try {
                if(imageView != null)
                    imageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(f)));
            } catch (FileNotFoundException e) {
            }
        }
        if(gameData.getGameMode() == 2){  // Modo 3
            TextView tvName1 = findViewById(R.id.tvNomePlayer1);
            tvName1.setText(gameData.getPlayerName(0));
            if(gameData.getPlayerNames().size() > 1) {
                TextView tvName2 = findViewById(R.id.tvNomePlayer2);
                tvName2.setText(gameData.getPlayerName(1));
                ((TextView)findViewById(R.id.tvPontosJogador2)).setText("0");
                ((TextView)findViewById(R.id.tvStrPontosJogador2)).setText(getString(R.string.strPontos));
            } else{
                ((TextView)findViewById(R.id.tvNomePlayer2)).setText("");
                ((TextView)findViewById(R.id.tvPontosJogador2)).setText("");
                ((TextView)findViewById(R.id.tvStrPontosJogador2)).setText("");
            }
            if(gameData.getPlayerNames().size() > 2) {
                TextView tvName3 = findViewById(R.id.tvNomePlayer3);
                tvName3.setText(gameData.getPlayerName(2));
                ((TextView)findViewById(R.id.tvPontosJogador3)).setText("0");
                ((TextView)findViewById(R.id.tvStrPontosJogador3)).setText(getString(R.string.strPontos));
            } else {
                ((TextView)findViewById(R.id.tvNomePlayer3)).setText("");
                ((TextView)findViewById(R.id.tvPontosJogador3)).setText("");
                ((TextView)findViewById(R.id.tvStrPontosJogador3)).setText("");
            }
        }

    }

    public void sendGameDataToClients(){
        for (int i = 0; i < GameData.MAX_CLIENTS; i++) {
            if(gameData.getGameSocket(i) != null){
                gameData.getGameOutputs(i).println(gameData.toStringJSONFormat());
                gameData.getGameOutputs(i).flush();
            }
        }
    }

    class RunnableThreadServer implements Runnable{

        private int player;

        public RunnableThreadServer(int player) {
            this.player = player;
        }

        @Override
        public void run() {
            try {
                //Envio do gameData inicial aos clientes
                sendGameDataToClients();
                //Loop de espera por jogadas dos clientes
                while (!Thread.currentThread().isInterrupted()) {
                    String jsonMove = gameData.getGameInput(player - 2).readLine();
                    JSONObject jsonObject = new JSONObject(jsonMove);
                    int row = (int)jsonObject.get("row");
                    int column = (int)jsonObject.get("column");
                    boolean onNotas = (boolean)jsonObject.get("onNotas");
                    boolean onApagar = (boolean)jsonObject.get("onApagar");
                    int value = (int)jsonObject.get("value");
                    if(gameData.getPlayer() == player) {
                        validateMove(row, column, value, onNotas, onApagar);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sudokuView.invalidate();
                            }
                        });
                        sendGameDataToClients();
                    }
                }
            } catch (Exception e) {
                /*procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Toast.makeText(getApplicationContext(),
                                R.string.game_finished, Toast.LENGTH_LONG)
                                .show();
                    }
                });*/
            }
        }
    }

    Thread clientCommunication = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                gameData.setGameInput(0, new BufferedReader(new InputStreamReader(gameData.getGameSocket(0).getInputStream())));
                gameData.setGameOutput(0, new PrintWriter(gameData.getGameSocket(0).getOutputStream()));
                //Enviar o nome e a foto do jogador ao servidor
                JSONObject jsonPlayerName = new JSONObject();
                jsonPlayerName.put("name", PlayerProfileActivity.getPlayerName(getApplicationContext()));
                gameData.getGameOutputs(0).println(jsonPlayerName.toString());
                gameData.getGameOutputs(0).flush();

                //Receber o GameData Inicial
                String gameDataJSON = gameData.getGameInput(0).readLine();
                JSONObject jsonObject = new JSONObject(gameDataJSON);
                gameData.updateThroughJSON(jsonObject);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sudokuView.invalidate();
                        initializaPlayerNames();
                        updatePlayersColors();
                        ((TextView)findViewById(R.id.tvTempoJogo)).setText("" + gameData.getPlayerTime());
                    }
                });

                //Terminar a dialog para começar a jogar
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        isProgressDialogActive = false;
                    }
                });

                while (!Thread.currentThread().isInterrupted()) {
                    gameDataJSON = gameData.getGameInput(0).readLine();
                    jsonObject = new JSONObject(gameDataJSON);
                    Boolean finished = jsonObject.optBoolean("finish", false);
                    if(finished){
                        Boolean winner = jsonObject.optBoolean("winner");
                        String name = jsonObject.optString("winnerName");
                        int time = jsonObject.optInt("time");
                        int numbers = jsonObject.optInt("numbersAchieved");
                        saveGameResultMode3(name, time, numbers);
                        showEndGameMessage(winner);
                    }
                    else {
                        //Atualizar o GameData e a view
                        gameData.updateThroughJSON(jsonObject);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sudokuView.invalidate();
                                initializaPlayerNames();
                                updatePlayersColors();
                                ((TextView) findViewById(R.id.tvPontosJogador1)).setText("" + gameData.getPlayerScore(1));
                                ((TextView) findViewById(R.id.tvPontosJogador2)).setText("" + gameData.getPlayerScore(2));
                                if (gameData.getPlayerNames().size() > 2)
                                    ((TextView) findViewById(R.id.tvPontosJogador3)).setText("" + gameData.getPlayerScore(3));
                                ((TextView) findViewById(R.id.tvTempoJogo)).setText("" + gameData.getPlayerTime());
                            }
                        });
                    }
                }
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        // finish();
                        // Toast.makeText(getApplicationContext(), R.string.game_finished, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    });

    private void showEndGameMessage(boolean isWinner){ ;
        if(isWinner) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = new AlertDialog.Builder(GameBoardActivity.this, R.style.AppCompatAlertDialogStyle)
                            .setTitle(R.string.strGanhou)
                            .setMessage(R.string.strTerminouJogo)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setCancelable(false)
                            .setPositiveButton(R.string.strOK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(gameData.isServidor()){
                                        serverCommunicationPlayer1.interrupt();
                                        if(serverCommunicationPlayer2 != null)
                                            serverCommunicationPlayer2.interrupt();
                                    } else {
                                        clientCommunication.interrupt();
                                    }
                                    finish();
                                }
                            }) //Ao clicar no botão voltar à página principal.
                            .create();
                    Button btn;
                    btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (btn != null) {
                        btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }
                    dialog.show();
                }
            });
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = new AlertDialog.Builder(GameBoardActivity.this, R.style.AppCompatAlertDialogStyle)
                            .setTitle(R.string.strPerder)
                            .setMessage(R.string.strPerdeuJogo)
                            .setCancelable(false)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton(R.string.strOK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(gameData.isServidor()){
                                        serverCommunicationPlayer1.interrupt();
                                        if(serverCommunicationPlayer2 != null)
                                            serverCommunicationPlayer2.interrupt();
                                    } else {
                                        clientCommunication.interrupt();
                                    }
                                    finish();
                                }
                            }) //Ao clicar no botão voltar à página principal.
                            .create();
                    Button btn;
                    btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (btn != null) {
                        btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }
                    dialog.show();
                }
            });
        }

    }

    private void validateMove(int row, int column, int value, boolean onNotas, boolean onApagar){
        if(!onApagar && !onNotas && gameData.getValue(row, column) == 0) {
            gameData.setValue(row, column, value);
            gameData.validateNumber(row, column);
            if(gameData.getValue(row, column) != 0){ //Se o número inserido for válido
                gameData.validateNotesAfterNewValidNumber(row, column);
                gameData.setPlayerOfInsertedNumber(row, column);
                gameData.setCorrectNumberTime();
                gameData.incrementPlayerScore();
                gameData.checkTerminateGame();
                if(gameData.isFinished()){
                    // =========== Gravar os resultados do jogo ===========
                    saveGameResultMode3();
                    // ====================================================
                    if(!(gameData.getGameMode() == 2 && gameData.getPlayerWinner() != 0)) {
                        showEndGameMessage(true);
                    } else {
                        showEndGameMessage(false);
                    }
                }
            }
        } else if(!onApagar && onNotas) {
            if(gameData.getPlayer() == 1) {
                if (gameData.getCellNote(row, column, value - 1) == 0) { //Verifica se o valor já está nas notas
                    gameData.setCellNote(row, column, value - 1, value); //Se não estiver coloca
                    gameData.validateNumber(row, column, value, 1);
                } else
                    gameData.setCellNote(row, column, value - 1, 0); //Se já estiver, retira
            } else if(gameData.getPlayer() == 2){
                if (gameData.getPlayer2CellNote(row, column, value - 1) == 0) { //Verifica se o valor já está nas notas
                    gameData.setPlayer2CellNote(row, column, value - 1, value); //Se não estiver coloca
                    gameData.validateNumber(row, column, value, 2);
                } else
                    gameData.setPlayer2CellNote(row, column, value - 1, 0); //Se já estiver, retira
            } else if(gameData.getPlayer() == 3){
                if (gameData.getPlayer3CellNote(row, column, value - 1) == 0) { //Verifica se o valor já está nas notas
                    gameData.setPlayer3CellNote(row, column, value - 1, value); //Se não estiver coloca
                    gameData.validateNumber(row, column, value, 3);
                } else
                    gameData.setPlayer3CellNote(row, column, value - 1, 0); //Se já estiver, retira
            }
        }
        else if(onApagar){
            if(gameData.getValue(row, column)>0 && gameData.getPlayerOfInsertedNumber(row, column) == gameData.getPlayer()) {
                gameData.setValue(row, column, 0);
                gameData.decrementPlayerScore();
            }
            else {
                if(gameData.getPlayer() == 1)
                    gameData.resetCellNotes(row, column); //Apaga todas as notas do jogador 1
                else if(gameData.getPlayer() == 2)
                    gameData.resetPlayer2CellNotes(row, column); //Apaga todas as notas do jogador 2
                else if(gameData.getPlayer() == 3)
                    gameData.resetPlayer3CellNotes(row, column); //Apaga todas as notas do jogador 3
            }
        }
        //invalidate(); // faz um refresh
    }

    /**
     * Gravar os resultados do jogo no modo 3
     */
    public void saveGameResultMode3(){
        int indexWinnerPlayer = gameData.getPlayerWinner();
        GameHistoryData ghd = new GameHistoryData(gameData.getPlayerName(indexWinnerPlayer), "M3", gameData.getGameTime(), gameData.getPlayerScore(indexWinnerPlayer + 1));
        GameHistoryViewModel ghvm = new GameHistoryViewModel(GameBoardActivity.this);
        ghvm.addNewGame(ghd);
        ghvm.saveHistory();

    }

    public void saveGameResultMode3(String winner, int time, int numbersAchive){
        GameHistoryData ghd = new GameHistoryData(winner, "M3", time, numbersAchive);
        GameHistoryViewModel ghvm = new GameHistoryViewModel(GameBoardActivity.this);
        ghvm.addNewGame(ghd);
        ghvm.saveHistory();

    }
}