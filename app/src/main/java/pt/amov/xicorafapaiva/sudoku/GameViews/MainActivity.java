package pt.amov.xicorafapaiva.sudoku.GameViews;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import pt.amov.xicorafapaiva.sudoku.R;

public class MainActivity extends AppCompatActivity {

    //Varíaveis referentes à captura da selfie
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri uri;
    Bitmap foto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClickSinglePlayer(View view) {
        Intent myIntent = new Intent(getBaseContext(),   CreditsActivity.class);
        startActivity(myIntent);
    }

    public void onClickMultiplayer(View view) {
    }

    public void onClickMultiplayerNetwork(View view) {
    }

    public void onClickCameraUser(View view) {
        // Preparar o URI para depois guardar o caminho da foto
        File diretorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imagem = new File(diretorio.getPath() + "/" + System.currentTimeMillis() + ".jpg");
        uri  = Uri.fromFile(imagem);

        //Inciar a Câmera e tirar uma foto
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE){
            if(resultCode == RESULT_OK){
                Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                sendBroadcast(scannerIntent);

                ImageView img = findViewById(R.id.ivTeste);
                foto = (Bitmap)data.getExtras().get("data");
                img.setImageBitmap(foto);
            }
        }
    }
}
