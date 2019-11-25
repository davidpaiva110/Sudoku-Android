package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import pt.amov.xicorafapaiva.sudoku.GameClasss.GameData;
import pt.amov.xicorafapaiva.sudoku.Player;
import pt.amov.xicorafapaiva.sudoku.R;

public class EditPlayerProfileActivity extends AppCompatActivity {

    //Varíaveis referentes à captura da selfie
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri uri;

    private Player player;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_player_profile);
        this.player = ViewModelProviders.of(this).get(Player.class);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/sudoku/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        this.player.setDirectory(directory);
        this.loadImageFromStorage(player.getDirPath());
        TextView tvPlayerName = findViewById(R.id.inputPlayerName);
        tvPlayerName.setText(player.getPlayerName());
    }


    public void onClickCameraUser(View view) {
        //Inciar a Câmera e tirar uma foto
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onClickGuardar(View view){
        // Guardar a informação do perfil
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE){
            if(resultCode == RESULT_OK){
                Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                sendBroadcast(scannerIntent);

                ImageView img = findViewById(R.id.imgPlayerPhoto);
                this.player.setFoto((Bitmap)data.getExtras().get("data"));
                img.setImageBitmap(player.getFoto());
                this.saveToInternalStorage( player.getFoto() );
            }
        }
    }

    //Guardar o estado atual
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView tvPlayerName = findViewById(R.id.inputPlayerName);
        tvPlayerName.setText(player.getPlayerName());
    }

    //Recuperar o estado anterior
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ImageView img = findViewById(R.id.imgPlayerPhoto);
        img.setImageBitmap(player.getFoto());
        TextView tvPlayerName = findViewById(R.id.inputPlayerName);
        tvPlayerName.setText(player.getPlayerName());
    }

    // Guardar a foto no InternalStorage
    private String saveToInternalStorage(Bitmap bitmapImage){
        //ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/sudoku/app_data/imageDir
        //File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(player.getDirectory(),"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
        } finally {
            try {
                fos.close();
            } catch (IOException e) {

            }
        }
        return player.getDirectory().getAbsolutePath();
    }

    //Ir buscar a imagem do internal Strorage
    private void loadImageFromStorage(String path)
    {
        try {
            File f=new File(path, "profile.jpg");
            this.player.setFoto( BitmapFactory.decodeStream(new FileInputStream(f)) );
            ImageView img = findViewById(R.id.imgPlayerPhoto);
            img.setImageBitmap(player.getFoto());
        }
        catch (FileNotFoundException e)
        {
        }
    }



}
