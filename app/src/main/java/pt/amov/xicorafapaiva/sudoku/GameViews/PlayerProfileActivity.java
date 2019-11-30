package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import pt.amov.xicorafapaiva.sudoku.Player;
import pt.amov.xicorafapaiva.sudoku.R;

public class PlayerProfileActivity extends AppCompatActivity {


    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_profile);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        this.player = ViewModelProviders.of(this).get(Player.class);

        loadImageFromStorage(directory.getAbsolutePath());
        loadFile();
        TextView tvPlayerName = findViewById(R.id.tvPlayerName);
        tvPlayerName.setText(player.getPlayerName());
    }


    //Onde está guardado o nome do jogador
    public void loadFile(){
        try {
            FileInputStream fis = getApplication().openFileInput("player.txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));
            String line= r.readLine();
            player.setPlayerName(line);
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Onde está guardada a foto do jogador
    private void loadImageFromStorage(String path)
    {
        try {
            File f=new File(path, "profile.jpg");
            this.player.setFoto( BitmapFactory.decodeStream(new FileInputStream(f)) );
            ImageView img = findViewById(R.id.playerPhotoProfile);
            img.setImageBitmap(player.getFoto());
        }
        catch (FileNotFoundException e)
        {
        }
    }
}
