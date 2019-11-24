package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import pt.amov.xicorafapaiva.sudoku.R;

public class ChooseClientServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_client_server);
    }

    public void onClickCliente(View view) {

        //finish();
    }

    public void onClickServidor(View view) {

        //finish();
    }
}
