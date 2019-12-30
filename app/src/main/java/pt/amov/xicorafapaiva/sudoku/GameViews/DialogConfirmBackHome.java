package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.R;

public class DialogConfirmBackHome extends DialogFragment {

    GameData gameData;
    Thread serverCommunicationPlayer1, serverCommunicationPlayer2, thTempo, clientCommunication;

    public DialogConfirmBackHome(GameData gameData, Thread serverCommunicationPlayer1, Thread serverCommunicationPlayer2, Thread thTempo, Thread clientCommunication) {
        this.gameData = gameData;
        this.serverCommunicationPlayer1 = serverCommunicationPlayer1;
        this.serverCommunicationPlayer2 = serverCommunicationPlayer2;
        this.thTempo = thTempo;
        this.clientCommunication = clientCommunication;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.strConfirmar)
                .setMessage(R.string.strDialogMSG)
                .setIcon(android.R.drawable.ic_menu_help)
                .setPositiveButton(R.string.strConfirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(gameData.getGameMode() == 2){
                            if(gameData.isServidor()){
                                thTempo.interrupt();
                                Thread thToMode1 = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < GameData.MAX_CLIENTS; i++) {
                                            if (gameData.getGameOutputs(i) != null) {
                                                JSONObject jsonToMode1 = new JSONObject();
                                                try {
                                                    jsonToMode1.put("changeToMode1", true);
                                                } catch (JSONException e) {
                                                }
                                                gameData.getGameOutputs(i).println(jsonToMode1.toString());
                                                gameData.getGameOutputs(i).flush();
                                            }
                                        }
                                        serverCommunicationPlayer1.interrupt();
                                        if (serverCommunicationPlayer2 != null)
                                            serverCommunicationPlayer2.interrupt();
                                    }
                                });
                                thToMode1.start();
                            } else {
                                Thread thToMode1 = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JSONObject jsonToMode1 = new JSONObject();
                                        try {
                                            jsonToMode1.put("changeToMode1", true);
                                        } catch (JSONException e) {
                                        }
                                        clientCommunication.interrupt();
                                        gameData.getGameOutputs(0).println(jsonToMode1.toString());
                                        gameData.getGameOutputs(0).flush();
                                        try {
                                            gameData.getGameOutputs(0).close();
                                            gameData.getGameInput(0).close();
                                            gameData.getGameSocket(0).close();
                                        } catch (IOException e) {
                                        }
                                    }
                                });
                                thToMode1.start();
                            }
                        }
                        getActivity().finish();
                    }
                })
                .setNegativeButton(R.string.strCancelar,null)
                .create();
        Button btn;
        btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if(btn != null){
            btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        return dialog;
    }
}
