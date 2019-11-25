package pt.amov.xicorafapaiva.sudoku;

import android.graphics.Bitmap;

import androidx.lifecycle.ViewModel;
import java.io.File;


public class Player extends ViewModel  {
    private Bitmap foto;
    private File directory;
    private String playerName;


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

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}


//falta guardar um fiocheiro com esta informação sobre o jogador e buscar cada vez que se inicia o jogo