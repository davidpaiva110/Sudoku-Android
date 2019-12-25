package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.app.ProgressDialog;
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
import android.view.View;

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

    //Flag para saber se é o modo SinglePlayer ou Multiplayer
    private int gameModeFlag = 0;   // 0 -> SinglePlayer    |    1 -> Multiplayer


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_difficulty);
        if(savedInstanceState == null)
            gameModeFlag = getIntent().getIntExtra("gameModeFlag", 0);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("pd", isProgressDialogActive);
        outState.putInt("gameMode", gameModeFlag);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isProgressDialogActive = savedInstanceState.getBoolean("pd");
        gameModeFlag = savedInstanceState.getInt("gameMode");
        if(isProgressDialogActive == true)
           createProgressDialog();
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

        startActivity(myIntent);
        pd.dismiss();
        finish();
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