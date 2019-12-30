package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.R;

public class DialogConfirmChangeToM1 extends DialogFragment {

    GameData gameData;
    Thread serverCommunicationPlayer1, serverCommunicationPlayer2, thTempo, clientCommunication;
    Intent intent;

    public DialogConfirmChangeToM1(Intent intent, GameData gameData, Thread srvComm1, Thread srvComm2, Thread thTempo, Thread clientCommunication) {
        this.gameData = gameData;
        this.serverCommunicationPlayer1 = srvComm1;
        this.serverCommunicationPlayer2 = srvComm2;
        this.intent = intent;
        this.thTempo = thTempo;
        this.clientCommunication = clientCommunication;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.strConfirmar)
                .setMessage(R.string.strPassarParaM1) //Alterar mensagens
                .setIcon(android.R.drawable.ic_menu_help)
                .setPositiveButton(R.string.strConfirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(gameData.getGameMode() == 2) {
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
                                        gameData.getGameOutputs(0).println(jsonToMode1.toString());
                                        gameData.getGameOutputs(0).flush();
                                    }
                                });
                                thToMode1.start();
                            }
                        }
                        if(!(gameData.getGameMode() == 2 && !gameData.isServidor())) {
                            getActivity().finish();
                            startActivity(intent);
                        }
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
