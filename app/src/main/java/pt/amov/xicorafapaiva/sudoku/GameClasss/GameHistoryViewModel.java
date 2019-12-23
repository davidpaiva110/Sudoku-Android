package pt.amov.xicorafapaiva.sudoku.GameClasss;

import android.content.Context;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class GameHistoryViewModel  {

    public static final String HISTORY_FILE_NAME ="historico.bin";
    public static final int MAX_GAMES_SAVED = 10;

    private ArrayList<GameHistoryData> lastGames;
    private Context context;


    public GameHistoryViewModel(Context context) {
        this.lastGames = new ArrayList<>();
        this.context = context;
        readHistory();
    }


    public void readHistory() {
        try {
            FileInputStream fis = context.openFileInput(HISTORY_FILE_NAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            ArrayList<GameHistoryData> array = (ArrayList<GameHistoryData>) is.readObject();
            lastGames = array;
            is.close();
            fis.close();
        } catch (Exception e) {
        }

    }

    public void addNewGame(GameHistoryData ghd){
        if(lastGames != null){
            if(lastGames.size() >= MAX_GAMES_SAVED)
                lastGames.remove(0);
            lastGames.add(ghd);
        }
    }

    public void saveHistory(){
        try {
            FileOutputStream fos = context.openFileOutput(HISTORY_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(lastGames);
            os.close();
            fos.close();
        } catch (Exception e) {
        }
    }

}
