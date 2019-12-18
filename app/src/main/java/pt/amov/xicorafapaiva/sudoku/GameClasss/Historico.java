package pt.amov.xicorafapaiva.sudoku.GameClasss;

public class Historico {

    private String vencedor;
    private int modoJogo;
    private int tempo;
    private int numerosDescobertos;

    /**
     *
     * @param vencedor - nome do vencedor do jogo
     * @param modoJogo - modo de jogo
     * @param x - Se modoJogo = 1 então x corresponde ao tempo | restantes modos corresponde aos numerosDescobertos
     */
    public Historico(String vencedor, int modoJogo, int x){
        this.vencedor = vencedor;
        this.modoJogo = modoJogo;
        if(modoJogo == 1)
            this.tempo = x;
        else
            this.numerosDescobertos = x;
    }

    public String getVencedor() {
        return vencedor;
    }

    public int getModoJogo() {
        return modoJogo;
    }

    public int getTempo() {
        return tempo;
    }

    public int getNumerosDescobertos() {
        return numerosDescobertos;
    }
}
