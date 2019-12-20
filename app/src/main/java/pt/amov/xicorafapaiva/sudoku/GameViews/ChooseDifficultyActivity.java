package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

    public int nr;
    public int nc;
    public ProgressDialog pd;
    private ArrayList<Integer> aa;
    private Handler h = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_difficulty);

    }

    public void onClickCFacil(View view) {
        pd = ProgressDialog.show(this, getString(R.string.strCarregarJogo), getString(R.string.strPorFavorAguarde), false, false);
//        pd = new ProgressDialog(getApplicationContext(), R.style.AppCompatAlertDialogStyle);
//        pd.setTitle(getString(R.string.strCarregarJogo));
//        pd.setMessage(getString(R.string.strPorFavorAguarde));
//        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        pd.setCancelable(false);
//        pd.show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // your code here
                aa = getSudokuBoard();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if(pd != null)
                            pd.dismiss();
                        startGameBoardActivity();
                    }
                });
            }
        });
        t.start();
    }

    public void startGameBoardActivity(){
        Intent myIntent = new Intent(getBaseContext(),   GameBoardActivity.class);
        myIntent.putExtra("board", aa);
        myIntent.putExtra("nr", nr);
        myIntent.putExtra("nc", nc);

        startActivity(myIntent);
        finish();
    }

    public void onClickMedio(View view) {

        //finish();
    }

    public void onClickDificil(View view) {

        //finish();
    }

    public ArrayList<Integer> getSudokuBoard(){
        ArrayList<Integer> board = null;
        String strJson = Sudoku.generate(15);
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
        Log.i("eu123", ""+nr);
        return al;
    }





}