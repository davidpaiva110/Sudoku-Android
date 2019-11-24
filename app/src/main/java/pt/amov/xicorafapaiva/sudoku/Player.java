package pt.amov.xicorafapaiva.sudoku;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;

import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.Serializable;

public class Player extends ViewModel  {
    private Bitmap foto;
    File directory;
    String dirPath;

    public Player() {
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public Bitmap getFoto() {
        return foto;
    }

    public void setFoto(Bitmap foto) {
        this.foto = foto;
    }

    public String getDirPath() {
        return directory.getAbsolutePath();
    }
}
