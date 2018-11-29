package com.example.perkoz.animalfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageDisplay;
    private ImageButton cameraButton, galleryButton, exitButton, infoButton;; //Buttons
    private FirebaseVisionImage imageFirebase; //FirebaseImage
    private Switch nightSwitch; //Switch Background
    private View layout; //Background
    private final int REQUEST_IMAGE_CAPTURE = 1, REQUEST_IMAGE_GALLERY = 2; //REQUESTS
    private TextView textPrediction, textDescription; //Text Fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Layout
        layout = (View) findViewById(R.id.background);
        nightSwitch = (Switch) findViewById(R.id.switchNight);
        nightSwitch.setOnClickListener(this);

        // Image View
        imageDisplay = (ImageView) findViewById(R.id.imageView);

        // Image Buttons
        cameraButton = (ImageButton) findViewById(R.id.imageButtonCamera);
        cameraButton.setOnClickListener(this);
        galleryButton = (ImageButton) findViewById(R.id.imageButtonGallery);
        galleryButton.setOnClickListener(this);
        exitButton = (ImageButton) findViewById(R.id.imageButtonExit);
        exitButton.setOnClickListener(this);
        infoButton = (ImageButton) findViewById(R.id.imageButtonInfo); // info button
        infoButton.setOnClickListener(this);
        // Text Fields
        textPrediction = (TextView) findViewById(R.id.textViewPrediction);
        textDescription = (TextView) findViewById(R.id.textViewDescription);



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
                if (nightSwitch.isChecked()) {
                    layout.setBackgroundColor(Color.YELLOW);
                } else {
                    layout.setBackgroundColor(Color.WHITE);
                }
            case R.id.imageButtonInfo:
                Intent intent = new Intent(MainActivity.this, Wikipedia.class);
                startActivity(intent);
        }
    }

    // -------------------- onActivity Result --------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                //PHOTO FROM CAMERA
                Bitmap bitmapImage = (Bitmap) data.getExtras().get("data");
                imageDisplay.setImageBitmap(bitmapImage);
                imageFirebase = FirebaseVisionImage.fromBitmap(bitmapImage);
                textPrediction.setText("Loading...");
                labelImagesCloud(imageFirebase);
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                //PHOTO FROM GALLERY
                Uri uri = data.getData();
                Bitmap bitmapImage = null;
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageDisplay.setImageBitmap(bitmapImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageDisplay.setImageBitmap(bitmapImage);
                imageFirebase = FirebaseVisionImage.fromBitmap(bitmapImage);
                textPrediction.setText("Loading...");
                labelImagesCloud(imageFirebase);
            }
        }
    }

    private void labelImagesCloud(FirebaseVisionImage image) {
        // [START set_detector_options_cloud]
        FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(3)
                .build();
        // [END set_detector_options_cloud]

        // [START get_detector_cloud]
        FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLabelDetector(options);
        // Or, to change the default settings:
        // FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance()
        //         .getVisionCloudLabelDetector(options);
        // [END get_detector_cloud]
        // [START run_detector_cloud]
        Task<List<FirebaseVisionCloudLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionCloudLabel> labels) {
                                        // Task completed successfully
                                        // [START_EXCLUDE]
                                        // [START get_labels_cloud]
                                        textPrediction.setText("");
                                        for (FirebaseVisionCloudLabel label : labels) {
                                            String text = label.getLabel();
                                            //textPrediction.setText(text + "\n");
                                            //textPrediction.setText(labels.get(0).getLabel() + "\n" + labels.get(0).getEntityId() + "\n" +labels.get(0).getConfidence() + "\n" );
                                            //textPrediction.append(labels.get(1).getLabel());
                                            String entityId = label.getEntityId();
                                            float confidence = label.getConfidence();
                                            textPrediction.append(text + " " + String.format("%.3f", confidence) + "\n");
                                        }
                                        // [END get_labels_cloud]
                                        // [END_EXCLUDE]
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        textPrediction.setText("Nieudana próba połączenia z serwerem.");
                                    }
                                });
        // [END run_detector_cloud]
    }
}
