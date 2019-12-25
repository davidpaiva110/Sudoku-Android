package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

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
            int nr = getIntent().getIntExtra("nr", 9);
            int nc = getIntent().getIntExtra("nc", 9);
            ArrayList<Integer> alBoard = getIntent().getIntegerArrayListExtra("board");
            int [][] tabuleiro = new int[nr][nc];
            int aux = 0;
            for(int r = 0; r < nr; r++) {
                for (int c = 0; c < nc; c++) {
                    tabuleiro[r][c] = alBoard.get(aux);
                    aux++;
                }
            }
            this.gameData.setBoard(tabuleiro);
            this.gameData.setGameMode(mode);
            FrameLayout flSudoku = findViewById(R.id.flSudoku);
            sudokuView = new Board(this, this.gameData);
            flSudoku.addView(sudokuView);
            btBackground = findViewById(R.id.btnNotas).getBackground();
            initializeButtons();
            setupTimer();
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
                                    }
                                    tvTempoJogo.setText("" + gameData.getPlayerTime());
                                    //Atualiza as cores dos nomes do jogador para destacar o jogador atual
                                    if(gameData.getPlayer() == 1){
                                        ((TextView)findViewById(R.id.tvNomePlayer1)).setTextColor(Color.rgb(0,0,128));
                                        ((TextView)findViewById(R.id.tvPontosJogador1)).setTextColor(Color.rgb(0,0,128));
                                        ((TextView)findViewById(R.id.tvStrPontosJogador1)).setTextColor(Color.rgb(0,0,128));
                                        ((TextView)findViewById(R.id.tvNomePlayer2)).setTextColor(Color.GRAY);
                                        ((TextView)findViewById(R.id.tvPontosJogador2)).setTextColor(Color.GRAY);
                                        ((TextView)findViewById(R.id.tvStrPontosJogador2)).setTextColor(Color.GRAY);

                                    }
                                    else if(gameData.getPlayer() == 2){
                                        ((TextView)findViewById(R.id.tvNomePlayer2)).setTextColor(Color.rgb(11, 102, 35));
                                        ((TextView)findViewById(R.id.tvPontosJogador2)).setTextColor(Color.rgb(11, 102, 35));
                                        ((TextView)findViewById(R.id.tvStrPontosJogador2)).setTextColor(Color.rgb(11, 102, 35));
                                        ((TextView)findViewById(R.id.tvNomePlayer1)).setTextColor(Color.GRAY);
                                        ((TextView)findViewById(R.id.tvPontosJogador1)).setTextColor(Color.GRAY);
                                        ((TextView)findViewById(R.id.tvStrPontosJogador1)).setTextColor(Color.GRAY);
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
        inflater.inflate(R.menu.menu_game_board_activity, menu);
        return true;
    }

    // Processamento das opções selecionadas no meu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.homeIcon:
                DialogConfirmBackHome dialog = new DialogConfirmBackHome();
                dialog.show(getSupportFragmentManager(),"idConfirmarDialog");
                return true;
            case R.id.solutionIcon:
                DialogConfirmShowSolution dialogSol = new DialogConfirmShowSolution(sudokuView);
                dialogSol.show(getSupportFragmentManager(), "idSolutionDialog");
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
        btn.setTextColor(Color.WHITE);
    }

    // onClick dos Números
    public void onNumberPress(View view) {
        Button btn = (Button) view;
        String stringNumber = btn.getText().toString();
        int number = Integer.parseInt(stringNumber);
        sudokuView.setSelectedValue(number);
        resetNumbersColor();
        btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btn.setTextColor(Color.WHITE);
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
            ((Button)findViewById(buttonsIDs[i])).setTextColor(Color.BLACK);
        }
    }

    public void onBtnNotas(View view) {
        if(sudokuView.isOnNotas()) {
            sudokuView.setOnNotas(false);
            view.setBackground(btBackground);
            ((Button)view).setTextColor(Color.BLACK);
        }else{
            sudokuView.setOnNotas(true);
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ((Button)view).setTextColor(Color.WHITE);
        }
    }

    public void onBtnApagar(View view) {
        if(sudokuView.isOnApagar()) {
            sudokuView.setOnApagar(false);
            view.setBackground(btBackground);
            ((Button)view).setTextColor(Color.BLACK);
        }else{
            sudokuView.setOnApagar(true);
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ((Button)view).setTextColor(Color.WHITE);
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
                ((Button)findViewById(buttonsIDs[i])).setTextColor(Color.WHITE);
            }
        }
        if(isOnNotas){
            Button btnNotas = (Button)findViewById(R.id.btnNotas);
            btnNotas.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            btnNotas.setTextColor(Color.WHITE);
        }
        if(isOnApagar){
            Button btnApagar = (Button)findViewById(R.id.btnApagar);
            btnApagar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            btnApagar.setTextColor(Color.WHITE);
        }
    }


}