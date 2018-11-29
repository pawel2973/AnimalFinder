package com.example.perkoz.animalfinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Wikipedia extends AppCompatActivity {

    private TextView textViewDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String url = "https://en.wikipedia.org/api/rest_v1/page/summary/Cat";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikipedia);
        textViewDescription = (TextView) findViewById(R.id.textViewWikipedia);
        textViewDescription.setMovementMethod(new ScrollingMovementMethod());
                volleyJsonObjectRequest(url);

    }

    // -------------------- wiki --------------------
    public void volleyJsonObjectRequest(String url){

        String REQUEST_TAG = " com.androidtutorialpoint.volleyJsonObjectRequest";


        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //response.toString());
                        String test = null;
                        try {
                            test = response.getString("extract");
                            textViewDescription.setText(test);
                        } catch (JSONException e) {
                            textViewDescription.setText("Nie znaleziono informacji");
                        }


                    }
                }, new Response.ErrorListener() {
            @Override

            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error: " + error.getMessage());

            }
        });

        // Adding JsonObject request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectReq,REQUEST_TAG);
    }

}
