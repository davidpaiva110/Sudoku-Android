package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

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
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.R;

public class GameBoardActivity extends AppCompatActivity {

    public static final int SECOND = 1000;
    private static final int PORT = 8899;
    private static final int MAX_CLIENTS = 2;

    private Board sudokuView;
    private Drawable btBackground;

    // ViewModel dos dados do Jogo
    private GameData gameData;

    //Estruturas para o modo 3
    private ProgressDialog pd;
    private boolean isProgressDialogActive = false;
    private Handler procMsg = new Handler();
    private ServerSocket serverSocket=null;
    private Socket[] gameSockets = null;
    private BufferedReader[] gameInputs;
    private PrintWriter[] gameOutputs;
    private boolean isServidor = false; //Indica se é servidor ou cliente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int mode = getIntent().getIntExtra("mode", 0);
        if(mode == 0)
            setContentView(R.layout.activity_game_board);
        else if(mode == 1)
            setContentView(R.layout.activity_game_board_m2);
        else if(mode == 2) {
            setContentView(R.layout.activity_game_board_m3);
            gameSockets = new Socket[MAX_CLIENTS];
            gameInputs = new BufferedReader[MAX_CLIENTS];
            gameOutputs = new PrintWriter[MAX_CLIENTS];
            for (int i = 0; i < MAX_CLIENTS; i++) {
                gameSockets[i] = null;
                gameInputs[i] = null;
                gameOutputs[i] = null;
            }
        }
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
                isServidor = getIntent().getBooleanExtra("isServidor", false);
                if(!(mode == 2 && !isServidor)) {
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
                    this.gameData.setGameMode(mode);
                }
            }
            FrameLayout flSudoku = findViewById(R.id.flSudoku);
            sudokuView = new Board(this, this.gameData);
            flSudoku.addView(sudokuView);
            btBackground = findViewById(R.id.btnNotas).getBackground();
            initializeButtons();
            if(gameData.getGameMode() != 2)
                setupTimer();
            initializaPlayerNames();
            if(mode == 2){
                if(isServidor) {
                    createProgressDialog();
                    isProgressDialogActive = true;
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                serverSocket = new ServerSocket(PORT);
                                for (int i = 0; i < MAX_CLIENTS; i++) {
                                    gameSockets[i] = serverSocket.accept();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), R.string.strNovoClienteLigado, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                serverSocket.close();
                                serverSocket = null;
                            } catch (SocketException ex){

                            }catch (Exception e) {
                                gameSockets = null;
                            }
                            procMsg.post(new Runnable() {
                                @Override
                                public void run() {
                                    pd.dismiss();
                                    isProgressDialogActive = false;
                                    if (gameSockets == null) {
                                        Toast.makeText(getApplicationContext(), R.string.strErroComunicacao, Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                    serverCommunication.start();
                                    setupTimer();
                                }
                            });
                        }
                    });
                    t.start();
                }
                else{  //Cliente Modo 3
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
                    gameSockets[0] = new Socket(serverIP, serverPORT);
                    Log.d("Paivaaa", "new Socket");
                } catch (Exception e) {
                    gameSockets[0] = null;
                }
                if (gameSockets[0] == null) {
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.str_erro_ligaçãoCliente + serverIP, Toast.LENGTH_LONG).show();
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

    Thread clientCommunication = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Log.d("Paivaaa", "clientCommunication: inicio do run");
                gameInputs[0] = new BufferedReader(new InputStreamReader(gameSockets[0].getInputStream()));
                gameOutputs[0] = new PrintWriter(gameSockets[0].getOutputStream());
                //Receber o GameData
                String gameDataJSON = gameInputs[0].readLine();
                JSONObject jsonObject = new JSONObject(gameDataJSON);
                int  gd = (int) jsonObject.get("gameData");
                Log.d("Paivaaa", "REcebi o gameborad");
                Log.d("Paivaaa", "Valor GameData:" + gd);

                //Enviar o nome e a foto do jogador ao servidor

                while (!Thread.currentThread().isInterrupted()) {

//                    String read = gameInputs[0].readLine();
//                    final int move = Integer.parseInt(read);
//                    Log.d("RPS", "Received: " + move);
//                    procMsg.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            moveOtherPlayer(move);
//                        }
//                    });
                }
            } catch (Exception e) {
                Log.d("Paivaaa", e.toString());
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

    private void setupTimer() {
        Thread thTempo = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!gameData.isFinished()) {
                    try {
                        Thread.sleep(SECOND);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView tvTempoJogo = findViewById(R.id.tvTempoJogo);
                                gameData.incrementGameTime();
                                if(gameData.getGameMode() == 0)
                                    tvTempoJogo.setText("" + gameData.getGameTime());
                                else if(gameData.getGameMode() == 1){
                                    gameData.decrementPlayerTime();
                                    if(gameData.getPlayer() == 1)
                                        ((TextView)findViewById(R.id.tvPontosJogador1)).setText("" + gameData.getPlayerScore(1));
                                    else if(gameData.getPlayer() == 2)
                                        ((TextView)findViewById(R.id.tvPontosJogador2)).setText("" + gameData.getPlayerScore(2));
                                    if(gameData.getPlayerTime() < 0){
                                        gameData.nextPlayer();
                                        sudokuView.postInvalidate();
                                    }
                                    tvTempoJogo.setText("" + gameData.getPlayerTime());
                                    //Atualiza as cores dos nomes do jogador para destacar o jogador atual
                                    if(gameData.getPlayer() == 1){
                                        ((TextView)findViewById(R.id.tvNomePlayer1)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer1));
                                        ((TextView)findViewById(R.id.tvPontosJogador1)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer1));
                                        ((TextView)findViewById(R.id.tvStrPontosJogador1)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer1));
                                        ((TextView)findViewById(R.id.tvNomePlayer2)).setTextColor(getResources().getColor(R.color.colorGray));
                                        ((TextView)findViewById(R.id.tvPontosJogador2)).setTextColor(getResources().getColor(R.color.colorGray));
                                        ((TextView)findViewById(R.id.tvStrPontosJogador2)).setTextColor(getResources().getColor(R.color.colorGray));

                                    }
                                    else if(gameData.getPlayer() == 2){
                                        ((TextView)findViewById(R.id.tvNomePlayer2)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer2));
                                        ((TextView)findViewById(R.id.tvPontosJogador2)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer2));
                                        ((TextView)findViewById(R.id.tvStrPontosJogador2)).setTextColor(getResources().getColor(R.color.colorNumbersPlayer2));
                                        ((TextView)findViewById(R.id.tvNomePlayer1)).setTextColor(getResources().getColor(R.color.colorGray));
                                        ((TextView)findViewById(R.id.tvPontosJogador1)).setTextColor(getResources().getColor(R.color.colorGray));
                                        ((TextView)findViewById(R.id.tvStrPontosJogador1)).setTextColor(getResources().getColor(R.color.colorGray));
                                    }
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        thTempo.start();
    }


    // Criação do Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        int mode = getIntent().getIntExtra("mode", 1);
        if(mode == 0)
            inflater.inflate(R.menu.menu_game_board_activity, menu);
        else if(mode == 1)
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
        outState.putSerializable("gameSockets", gameSockets);
        outState.putSerializable("clientInputs", gameInputs);
        outState.putSerializable("clientOutputs", gameOutputs);
        outState.putBoolean("isServidor", isServidor);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int selectedValue = savedInstanceState.getInt("selectedValue");
        isProgressDialogActive = savedInstanceState.getBoolean("pd");
        isServidor = savedInstanceState.getBoolean("isServidor");
        gameSockets = (Socket[]) savedInstanceState.getSerializable("gameSockets");
        gameInputs = (BufferedReader[]) savedInstanceState.getSerializable("clientInputs");
        gameOutputs = (PrintWriter[]) savedInstanceState.getSerializable("clientOutputs");
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
        if(isProgressDialogActive == true)
            createProgressDialog();
    }

    public void createProgressDialog(){
        String ip = getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setTitle(getString(R.string.strEsperarClientes));
        pd.setMessage(getString(R.string.strIniciarJogo) + "\n(IP: " + ip + ")");
        pd.setCancelable(false);
        pd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.strCancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
                if(gameSockets[0] != null) { //Se houver pelo menos 1 cliente
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
        }

    }

    Thread serverCommunication = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                for (int i = 0; i < MAX_CLIENTS; i++) {
                    if(gameSockets[i] != null){
                        //Criação dos inputs e outputs
                        gameInputs[i] = new BufferedReader(new InputStreamReader(gameSockets[i].getInputStream()));
                        gameOutputs[i] = new PrintWriter(gameSockets[i].getOutputStream());
                        //Envio do gameData inicial
                        gameOutputs[i].println(gameData.toStringJSONFormat());
                        gameOutputs[i].flush();
                        Log.i("RAFAAA", "toStringJSONFormat: " + gameData.toStringJSONFormat());

                    }
                }
/*
                while (!Thread.currentThread().isInterrupted()) {
                    String read = input.readLine();
                    final int move = Integer.parseInt(read);
                    Log.d("RPS", "Received: " + move);
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            moveOtherPlayer(move);
                        }
                    });
                }*/
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
    });
}