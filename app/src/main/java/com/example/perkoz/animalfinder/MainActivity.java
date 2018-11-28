package com.example.perkoz.animalfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;

/* API KEY */
/*  */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView image;
    private ImageButton cameraButton, galleryButton, exitButton;
    private Switch nightSwitch;
    private View layout;
    //private static final int SELECT_PICTURE = 1;
    private final int REQUEST_IMAGE_CAPTURE = 1, REQUEST_IMAGE_GALLERY = 2;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageView);
        text = (TextView) findViewById(R.id.textViewDescription);
        cameraButton = (ImageButton) findViewById(R.id.imageButtonCamera);
        cameraButton.setOnClickListener(this);
        galleryButton = (ImageButton) findViewById(R.id.imageButtonGallery);
        galleryButton.setOnClickListener(this);
        exitButton = (ImageButton) findViewById(R.id.imageButtonExit);
        exitButton.setOnClickListener(this);
        layout = (View)findViewById(R.id.background);
        nightSwitch = (Switch) findViewById(R.id.switchNight);
        nightSwitch.setOnClickListener(this);
    }

    // -------------------- onClick Events --------------------
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageButtonCamera:
                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (iCamera.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(iCamera, REQUEST_IMAGE_CAPTURE);
                }
                break;
            case R.id.imageButtonGallery:
                Intent iGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                iGallery.setType("image/*");
                startActivityForResult(iGallery, REQUEST_IMAGE_GALLERY);
                break;
            case R.id.imageButtonExit:
                finish();
                System.exit(0);
            case R.id.switchNight:
                if(nightSwitch.isChecked()){
                    layout.setBackgroundColor(Color.YELLOW);
                }else{
                    layout.setBackgroundColor(Color.WHITE);
                }
        }
    }

    // -------------------- onActivity Result --------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(bitmap);
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                Uri uri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.setImageBitmap(bitmap);
            }
        }
    }
}
