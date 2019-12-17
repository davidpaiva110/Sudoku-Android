package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.Player;
import pt.amov.xicorafapaiva.sudoku.R;

public class GameBoardActivity extends AppCompatActivity {

    private Board sudokuView;
    private GameData gameData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        FrameLayout flSudoku = findViewById(R.id.flSudoku);
        sudokuView = new Board(this);
        flSudoku.addView(sudokuView);

        this.gameData = ViewModelProviders.of(this).get(GameData.class);
    }


    // onClick dos Números
    public void onNumberPress(View view) {
        Button btn = (Button) view;
        String stringNumber = btn.getText().toString();
        int number = Integer.parseInt(stringNumber);
    }
}
