package com.example.perkoz.animalfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private View layout; // Background
    private Switch nightSwitch; // Switch Background
    private ImageView imageDisplay; // Displayed Image
    private TextView textPrediction; // Predicted names
    private ImageButton cameraButton, galleryButton, exitButton, infoButton; // APP Buttons


    private final int REQUEST_IMAGE_CAPTURE = 1, REQUEST_IMAGE_GALLERY = 2; // REQUESTS
    private FirebaseVisionImage imageFirebase; // Firebase Vision Image
    private String animalName;  // Predicted animal name
    private Bitmap bitmapImage; // User image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Default content
        animalName = "hunter";
        bitmapImage = BitmapFactory.decodeResource(getResources(), R.drawable.deerhunter);
        bitmapImage = resizeImage(bitmapImage);

        // Layout
        layout = (View) findViewById(R.id.background);
        nightSwitch = (Switch) findViewById(R.id.switchNight);
        nightSwitch.setOnClickListener(this);

        // Image View
        imageDisplay = (ImageView) findViewById(R.id.imageView);
        imageDisplay.setImageBitmap(bitmapImage);

        // Image Buttons
        cameraButton = (ImageButton) findViewById(R.id.imageButtonCamera);
        cameraButton.setOnClickListener(this);
        galleryButton = (ImageButton) findViewById(R.id.imageButtonGallery);
        galleryButton.setOnClickListener(this);
        exitButton = (ImageButton) findViewById(R.id.imageButtonExit);
        exitButton.setOnClickListener(this);
        infoButton = (ImageButton) findViewById(R.id.imageButtonInfo);
        infoButton.setOnClickListener(this);

        // Text Fields
        textPrediction = (TextView) findViewById(R.id.textViewPrediction);
    }

    // -------------------- onClick Events --------------------
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageButtonCamera:
                // Camera Support
                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (iCamera.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(iCamera, REQUEST_IMAGE_CAPTURE);
                }
                break;
            case R.id.imageButtonGallery:
                // Gallery Support
                Intent iGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                iGallery.setType("image/*");
                startActivityForResult(iGallery, REQUEST_IMAGE_GALLERY);
                break;
            case R.id.imageButtonExit:
                finish();
                System.exit(0);
            case R.id.switchNight:
                // Switch Support
                if (nightSwitch.isChecked()) {
                    layout.setBackground(getDrawable(R.drawable.starfield_background));
                } else {
                    layout.setBackgroundResource(R.color.colorPrimary);
                }
                break;
            case R.id.imageButtonInfo:
                // Wikipedia Support
                // Change Image bitmap to Byte Array (Better format to send between activities
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Intent intent = new Intent(MainActivity.this, Wikipedia.class);
                intent.putExtra("animalname", animalName); // add animal name to new activity
                intent.putExtra("imagedisplay", byteArray); // add image to new activity
                startActivity(intent);
                break;
        }
    }

    // -------------------- onActivity Result --------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                //PHOTO FROM CAMERA
                bitmapImage = (Bitmap) data.getExtras().get("data");
                imageDisplay.setImageBitmap(bitmapImage);
                bitmapImage = resizeImage(bitmapImage);
                imageFirebase = FirebaseVisionImage.fromBitmap(bitmapImage);
                textPrediction.setText("Loading...");
                labelImagesCloud(imageFirebase);
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                //PHOTO FROM GALLERY
                Uri uri = data.getData();
                bitmapImage = null;
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageDisplay.setImageBitmap(bitmapImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageDisplay.setImageBitmap(bitmapImage);
                bitmapImage = resizeImage(bitmapImage);
                imageFirebase = FirebaseVisionImage.fromBitmap(bitmapImage);
                textPrediction.setText("Loading...");
                labelImagesCloud(imageFirebase);
            }
        }
    }

    private Bitmap resizeImage(Bitmap image) {
        float aspectRatio = image.getWidth() /
                (float) image.getHeight();
        int width = 480;
        int height = Math.round(width / aspectRatio);

        image = Bitmap.createScaledBitmap(
                image, width, height, false);
        
        return image;
    }

    private void labelImagesCloud(FirebaseVisionImage image) {
        // [START set_detector_options_cloud]
        FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(5)
                .build();
        // [END set_detector_options_cloud]

        // [START get_detector_cloud]
        FirebaseVisionCloudLabelDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLabelDetector(options);
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
                                            animalName = labels.get(0).getLabel();
                                        }
                                        // [END get_labels_cloud]
                                        // [END_EXCLUDE]
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        textPrediction.setText("An unsuccessful attempt to connect to the server. Check your Internet connection.");
                                    }
                                });
        // [END run_detector_cloud]
    }
}
