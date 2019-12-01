package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import pt.amov.xicorafapaiva.sudoku.R;

public class GameBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);
    }
}
