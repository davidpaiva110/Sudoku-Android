package pt.amov.xicorafapaiva.sudoku.GameViews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import pt.amov.xicorafapaiva.sudoku.R;

public class DialogConfirmShowSolution extends DialogFragment {

    Board board;

    public DialogConfirmShowSolution(Board board) {
        this.board = board;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.strConfirmar)
                .setMessage(R.string.strSolutionMSG)
                .setIcon(android.R.drawable.ic_menu_help)
                .setPositiveButton(R.string.strConfirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        board.getGameData().showSolution();
                        board.invalidate();
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
    }}
