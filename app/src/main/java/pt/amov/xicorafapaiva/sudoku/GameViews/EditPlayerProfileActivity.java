package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    Bitmap foto;
    private String dirPhotosPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_player_profile);
    }


    public void onClickCameraUser(View view) {
        //Inciar a Câmera e tirar uma foto
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onClickGuardar(View view){
        //Guardar a informação do perfil
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
                foto = (Bitmap)data.getExtras().get("data");
                img.setImageBitmap(foto);
                this.dirPhotosPath = this.saveToInternalStorage(foto);
            }
        }
    }



    //Guardar o estado atual
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
       // outState.putSerializable("userPic", foto);
        outState.putString("dirPath", this.dirPhotosPath);
    }

    //Recuperar o estado anterior
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.dirPhotosPath = savedInstanceState.getString("dirPath");
        this.loadImageFromStorage(this.dirPhotosPath);

    }

    // Guardar a foto no InternalStorage
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/sudoku/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {

            }
        }
        return directory.getAbsolutePath();
    }

    //Ir buscar a imagem do internal Strorage
    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "profile.jpg");
            foto = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img = findViewById(R.id.imgPlayerPhoto);
            img.setImageBitmap(foto);
        }
        catch (FileNotFoundException e)
        {
        }
    }


}
