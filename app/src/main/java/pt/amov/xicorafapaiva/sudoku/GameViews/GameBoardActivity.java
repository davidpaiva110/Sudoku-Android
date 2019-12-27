package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.R;
import pt.isec.ans.sudokulibrary.Sudoku;

public class GameBoardActivity extends AppCompatActivity {

    public static final int SECOND = 1000;

    private Board sudokuView;
    private Drawable btBackground;

    // ViewModel dos dados do Jogo
    private GameData gameData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int mode = getIntent().getIntExtra("mode", 1);
        if(mode == 0)
            setContentView(R.layout.activity_game_board);
        else if(mode == 1)
            setContentView(R.layout.activity_game_board_m2);
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
                //Player1 Name
                gameData.addPlayerName(PlayerProfileActivity.getPlayerName(this));
                //Player2 Name
                if(mode == 1)
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
            FrameLayout flSudoku = findViewById(R.id.flSudoku);
            sudokuView = new Board(this, this.gameData);
            flSudoku.addView(sudokuView);
            btBackground = findViewById(R.id.btnNotas).getBackground();
            initializeButtons();
            setupTimer();
            initializaPlayerNames();
        }
    }

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
        outState.putBoolean("onNotas", sudokuView.getOnNotas());
        outState.putInt("selectedValue", sudokuView.getSelectedValue());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int selectedValue = savedInstanceState.getInt("selectedValue");
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


}