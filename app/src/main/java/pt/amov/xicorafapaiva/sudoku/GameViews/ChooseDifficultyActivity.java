package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import pt.amov.xicorafapaiva.sudoku.R;
import pt.isec.ans.sudokulibrary.Sudoku;

public class ChooseDifficultyActivity extends AppCompatActivity {

    // Levels
    public static final int EASY =  7;
    public static final int MEDIUM = 13;
    public static final int HARD = 17;

    private int nr;
    private int nc;
    private ProgressDialog pd;
    private ArrayList<Integer> aa;
    private Handler h = new Handler();
    private boolean isProgressDialogActive = false;
    private boolean isPlayerNameDialogActive = false;

    //Flag para saber se é o modo SinglePlayer ou Multiplayer ou Jogo em Rede
    private int gameModeFlag = 0;   // 0 -> SinglePlayer    |    1 -> Multiplayer     |    2 -> Jogo Em Rede
    private EditText editName;
    private String namePlayer2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_difficulty);
        if(savedInstanceState == null) {
            gameModeFlag = getIntent().getIntExtra("gameModeFlag", 0);
            namePlayer2 = getString(R.string.strJogador_2);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("pd", isProgressDialogActive);
        outState.putBoolean("pdPlayerName", isPlayerNameDialogActive);
        outState.putInt("gameMode", gameModeFlag);
        outState.putIntegerArrayList("aa", aa);
        outState.putInt("nr", nr);
        outState.putInt("nc", nc);
        if(editName != null)
            namePlayer2 = editName.getText().toString();
        outState.putString("Player2Name", namePlayer2);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isProgressDialogActive = savedInstanceState.getBoolean("pd");
        isPlayerNameDialogActive = savedInstanceState.getBoolean("pdPlayerName");
        gameModeFlag = savedInstanceState.getInt("gameMode");
        aa = savedInstanceState.getIntegerArrayList("aa");
        nr = savedInstanceState.getInt("nr");
        nc = savedInstanceState.getInt("nc");
        namePlayer2 = savedInstanceState.getString("Player2Name");
        if(isProgressDialogActive == true)
           createProgressDialog();
        if(isPlayerNameDialogActive == true)
            createDialogPlayer2Name();
    }

    public void onClickCFacil(View view) {
        createProgressDialog();
        isProgressDialogActive = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                aa = getSudokuBoard(EASY);
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        isProgressDialogActive = false;
                        startGameBoardActivity();
                    }
                });
            }
        });
        t.start();
    }

    public void onClickMedio(View view) {
        createProgressDialog();
        isProgressDialogActive = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                aa = getSudokuBoard(MEDIUM);
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        isProgressDialogActive = false;
                        startGameBoardActivity();
                    }
                });
            }
        });
        t.start();
    }

    public void onClickDificil(View view) {
        createProgressDialog();
        isProgressDialogActive = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                aa = getSudokuBoard(HARD);
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        isProgressDialogActive = false;
                        startGameBoardActivity();
                    }
                });
            }
        });
        t.start();
    }

    public void startGameBoardActivity(){
        Intent myIntent;
        //if(gameModeFlag == 1)
            //myIntent = new Intent(getBaseContext(),   GameBoardM2Activity.class);
        //else
            myIntent = new Intent(getBaseContext(),   GameBoardActivity.class);
        myIntent.putExtra("board", aa);
        myIntent.putExtra("nr", nr);
        myIntent.putExtra("nc", nc);
        myIntent.putExtra("mode", gameModeFlag);
        if(pd != null && pd.isShowing())
            pd.dismiss();
        if(gameModeFlag == 1){
            //Pedir Nome do Jogador
            createDialogPlayer2Name();
        }else{
            startActivity(myIntent);
            finish();
        }

    }

    /**
     *  Alert Dialog para pedir o nome do Jogador 2 -> Modo Multiplayer
     */
    public void createDialogPlayer2Name(){
        editName = new EditText(new ContextThemeWrapper(this, R.style.AppCompatAlertDialogStyle));
        final AlertDialog ad = new AlertDialog.Builder(this).setTitle("Jogador 2")
                .setView(editName)
                .setPositiveButton(R.string.strConfirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(getBaseContext(),   GameBoardActivity.class);
                        myIntent.putExtra("board", aa);
                        myIntent.putExtra("nr", nr);
                        myIntent.putExtra("nc", nc);
                        myIntent.putExtra("mode", gameModeFlag);
                        myIntent.putExtra("player2Name", editName.getText().toString());
                        startActivity(myIntent);
                        finish();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .create();
        editName.setMaxLines(1);
        editName.setText(namePlayer2);
        Button btn;
        btn = ad.getButton(AlertDialog.BUTTON_POSITIVE);
        if(btn != null){
            btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        ad.show();
        isPlayerNameDialogActive = true;
    }


    public void createProgressDialog(){
        pd = ProgressDialog.show(this, getString(R.string.strCarregarJogo), getString(R.string.strPorFavorAguarde), false, false);
    }


    public ArrayList<Integer> getSudokuBoard(int level){
        ArrayList<Integer> board = null;
        String strJson = Sudoku.generate(level);
        try{
            JSONObject json = new JSONObject(strJson);
            if(json.optInt("result", 0) == 1){
                JSONArray jsonArray = json.getJSONArray("board");
                board = convert(jsonArray);

            }
        } catch (Exception e){

        }
        return  board;
    }

    private ArrayList<Integer> convert(JSONArray jsonArray) {
        int [][] array = null;
        ArrayList<Integer> al = new ArrayList<>();

        int rows = 0, columns = 0;
        try {
            rows = jsonArray.length();
            columns = 0;
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

        this.nr = rows;
        this.nc = columns;
        for (int r = 0; r < rows; r++){
            for(int c = 0; c < columns; c++){
                al.add(array[r][c]);
            }
        }
        return al;
    }

}