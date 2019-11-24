package pt.amov.xicorafapaiva.sudoku;

import android.graphics.Bitmap;

import androidx.lifecycle.ViewModel;

import java.io.Serializable;

public class Player extends ViewModel  {
    private Bitmap foto;

    public Player() {
    }

    public Bitmap getFoto() {
        return foto;
    }

    public void setFoto(Bitmap foto) {
        this.foto = foto;
    }
}
