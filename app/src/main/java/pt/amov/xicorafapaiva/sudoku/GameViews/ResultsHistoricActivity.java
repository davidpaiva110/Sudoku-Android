package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.amov.xicorafapaiva.sudoku.GameClasss.Historico;
import pt.amov.xicorafapaiva.sudoku.R;

public class ResultsHistoricActivity extends AppCompatActivity {

    String[] strings = {"David", "Rafael", "Francisco"};
    ArrayList<Historico> historicos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_historic);

        historicos = new ArrayList<>();
        historicos.add(new Historico("Paiva", 1, 50));
        historicos.add(new Historico("Rafael", 2, 2));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));
        historicos.add(new Historico("Francisco", 1, 1));

        ListView lv = findViewById(R.id.list);
        lv.setAdapter(new MyAdapter());

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(ResultsHistoricActivity.this,">> "+i+";"+l+":"+strings[(int)l],
//                        Toast.LENGTH_SHORT).show();
//            }
//        });

    }


    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return historicos.size();
        }

        @Override
        public Object getItem(int position) {
            return historicos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position * 1000;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layout = getLayoutInflater().inflate(R.layout.list_item,null);

            Historico hist = (Historico) getItem(position);
            String vendedor = hist.getVencedor();
            int modo = hist.getModoJogo();
            int valor;
            if(modo == 1)
                valor = hist.getTempo();
            else
                valor = hist.getNumerosDescobertos();

            ((TextView)layout.findViewById(R.id.text0)).setText(vendedor);
            ((TextView)layout.findViewById(R.id.text1)).setText(""+modo);
            ((TextView)layout.findViewById(R.id.text2)).setText(""+valor);

            return layout;
        }
    }
}
