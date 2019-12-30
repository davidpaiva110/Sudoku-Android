package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import pt.amov.xicorafapaiva.sudoku.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //Criação do Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    //Processamento das opções selecionadas no meu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
// Handle item selection
        switch (item.getItemId()) {
            case R.id.optionEditarPerfil:
                EditPlayerProfile();
                return true;
            case R.id.optionPerfilJogador:
                PlayerProfile();
                return true;
            case R.id.optionCredits:
                Credits();
                return true;
            case R.id.optionHistorico:
                Historic();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void Historic() {
        Intent myIntent = new Intent(getBaseContext(), ResultsHistoricActivity.class);
        startActivity(myIntent);
    }

    private void Credits() {
        Intent myIntent = new Intent(getBaseContext(), CreditsActivity.class);
        startActivity(myIntent);
    }

    private void PlayerProfile() {
        Intent myIntent = new Intent(getBaseContext(),   PlayerProfileActivity.class);
        startActivity(myIntent);
    }

    private void EditPlayerProfile() {
       Intent myIntent = new Intent(getBaseContext(),   EditPlayerProfileActivity.class);
      startActivity(myIntent);
    }


    public void onClickSinglePlayer(View view) {
        Intent myIntent = new Intent(getBaseContext(),   ChooseDifficultyActivity.class);
        myIntent.putExtra("gameModeFlag", 0);
        startActivity(myIntent);
    }

    public void onClickMultiplayer(View view) {
        Intent myIntent = new Intent(getBaseContext(),   ChooseDifficultyActivity.class);
        myIntent.putExtra("gameModeFlag", 1);
        startActivity(myIntent);
    }

    public void onClickMultiplayerNetwork(View view) {
        Intent myIntent = new Intent(getBaseContext(),   ChooseClientServerActivity.class);
        startActivity(myIntent);
    }




}
