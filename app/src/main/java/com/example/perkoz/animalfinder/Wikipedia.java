package com.example.perkoz.animalfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class Wikipedia extends AppCompatActivity {

    private String animalName; // Animal name
    private TextView textViewDescription; // Animal description
    private ImageView imageDisplay; // Displayed image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikipedia);

        Intent intent = getIntent();
        // Receive image from MainActivity as byteArray
        byte[] byteArray = getIntent().getByteArrayExtra("imagedisplay");
        Bitmap imageWikipedia = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        // Receive animal name from MainActivity
        animalName = intent.getExtras().getString("animalname");
        String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + animalName;

        textViewDescription = (TextView) findViewById(R.id.textViewWikipedia);
        textViewDescription.setMovementMethod(new ScrollingMovementMethod());
        volleyJsonObjectRequest(url);

        imageDisplay = (ImageView) findViewById(R.id.imageViewWikipedia);
        imageDisplay.setImageBitmap(imageWikipedia);
    }

    // -------------------- wiki --------------------
    public void volleyJsonObjectRequest(String url) {

        String REQUEST_TAG = " com.androidtutorialpoint.volleyJsonObjectRequest";

        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //response.toString());
                        String text = null;
                        try {
                            text = response.getString("extract");
                            textViewDescription.setText(text);
                            textViewDescription.append("\n\nPowered by wikipedia.org");
                        } catch (JSONException e) {
                            textViewDescription.setText("Sorry. No information found about " + animalName +" on wikipedia.org");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override

            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error: " + error.getMessage());
                textViewDescription.setText("Sorry. No information found about " + animalName +" on wikipedia.org");
            }
        });

        // Adding JsonObject request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectReq, REQUEST_TAG);

    }
}
