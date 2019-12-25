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

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.R;

public class DialogConfirmChangeToM1 extends DialogFragment {

    public static final int GAME_MODE1 = 0;

    Intent intent;
    GameData gameData;

    public DialogConfirmChangeToM1(GameData gameData, Intent intent) {
        this.gameData = gameData;
        this.intent = intent;
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
                        gameData.setGameMode(GAME_MODE1);
                        gameData.setPlayer(1);
                        getActivity().finish();
                        startActivity(intent);
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
