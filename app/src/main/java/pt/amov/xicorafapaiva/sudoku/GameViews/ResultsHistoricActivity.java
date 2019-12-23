package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameHistoryData;
import pt.amov.xicorafapaiva.sudoku.GameClasss.GameHistoryViewModel;
import pt.amov.xicorafapaiva.sudoku.R;

public class ResultsHistoricActivity extends AppCompatActivity {

    ArrayList<GameHistoryData> historicos;
    ArrayList<GameHistoryData> historicosInvertido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_historic);

        historicos = new ArrayList<>();
        historicosInvertido = new ArrayList<>();
        readHistory();
        invertHistorico();

        ListView lv = findViewById(R.id.list);
        lv.setAdapter(new MyAdapter());
    }

    /**
     * Vai buscar o arraylist do histórico à memória.
     */
    public void readHistory() {
        try {
            FileInputStream fis = getApplicationContext().openFileInput(GameHistoryViewModel.HISTORY_FILE_NAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            ArrayList<GameHistoryData> array = (ArrayList<GameHistoryData>) is.readObject();
            historicos = array;
            is.close();
            fis.close();
        } catch (Exception e) {
        }

    }

    /**
     * Ordena o Histórico do mais recente para o mais antigo
     */
    public void invertHistorico(){
        for(int i=historicos.size()-1; i >= 0; i--){
            historicosInvertido.add(historicos.get(i));
        }
    }


    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return historicosInvertido.size();
        }

        @Override
        public Object getItem(int position) {
            return historicosInvertido.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position * 1000;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layout = getLayoutInflater().inflate(R.layout.list_item,null);

            GameHistoryData hist = (GameHistoryData) getItem(position);
            String vendedor = hist.getWiner();
            String modo = hist.getGameMode();
            int valor = hist.getTime();


            ((TextView)layout.findViewById(R.id.text0)).setText(vendedor);
            ((TextView)layout.findViewById(R.id.text1)).setText(""+modo);
            ((TextView)layout.findViewById(R.id.text2)).setText(""+valor);

            return layout;
        }
    }
}
