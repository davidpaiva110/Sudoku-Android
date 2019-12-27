package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pt.amov.xicorafapaiva.sudoku.R;

public class ChooseClientServerActivity extends AppCompatActivity {

    private static final int PORT = 8899;
    private static final int PORTEmulators= 9988; // Para testar com emuladores
    public static final int GAME_MODE_3 = 2;

    private String ipServer = "10.0.2.2";
    private EditText editIP;
    private boolean isDialogIP = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_client_server);
        /**
         * Verificar se existe ligação à internet
         */
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, R.string.str_erro_ligacao_net, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(editIP != null)
            ipServer = editIP.getText().toString();
        outState.putString("ipServer", ipServer);
        outState.putBoolean("isDialogIP", isDialogIP);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ipServer = savedInstanceState.getString("ipServer");
        createDialogIP();
    }


    public void onClickCliente(View view) {
        createDialogIP();
        //finish();
    }

    public void onClickServidor(View view) {
        Intent myIntent = new Intent(getBaseContext(),   ChooseDifficultyActivity.class);
        myIntent.putExtra("gameModeFlag", GAME_MODE_3);
        myIntent.putExtra("isServidor", true);
        startActivity(myIntent);
        finish();
    }


    public void createDialogIP(){
        isDialogIP = true;
        editIP = new EditText(new ContextThemeWrapper(this, R.style.AppCompatAlertDialogStyle));
        final AlertDialog ad = new AlertDialog.Builder(this).setTitle(R.string.str_ip_do_servidor)
                .setView(editIP)
                .setPositiveButton(R.string.strConfirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent  = new Intent(getBaseContext(),   GameBoardActivity.class);
                        myIntent.putExtra("serverIP", editIP.getText().toString());
                        myIntent.putExtra("serverPORT", PORT);
                        myIntent.putExtra("mode", GAME_MODE_3);
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
        editIP.setMaxLines(1);
        editIP.setText(ipServer);
        ad.show();
    }


}
