package pt.amov.xicorafapaiva.sudoku.GameClasss;

import androidx.lifecycle.ViewModel;

import java.io.Serializable;

public class GameData extends ViewModel implements Serializable {

    private String teste;

    public GameData() {

    }

    public String getTeste(){
        return teste;
    }


    public void setTeste(String teste) {
        this.teste = teste;
    }
}
