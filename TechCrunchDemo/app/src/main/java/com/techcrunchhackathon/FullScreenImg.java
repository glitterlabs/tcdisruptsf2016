package com.techcrunchhackathon;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.techcrunchhackathon.utils.AppHelper;
import com.techcrunchhackathon.utils.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FullScreenImg extends AppCompatActivity {

    private static final String API_KEY = "NWhnKnq8GTrx1zT4yV3_sQ";
    private ImageView imgScan, imgLoad;
    private TranslateAnimation mAnimation;
    private RequestQueue requestQueue;
    private String image_requests = "https://api.cloudsightapi.com/image_requests";
    private String image_responses = "https://api.cloudsightapi.com/image_responses/";
    private ImageView imgCaptured;
    private String img;
    private byte[] byteArray;
    private Bitmap bmap;
    private String status;
    TextView txtName;
    private CardView cardResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_img);
        imgScan = (ImageView) findViewById(R.id.imgScan);
        imgCaptured = (ImageView) findViewById(R.id.imgCaptured);
        cardResult = (CardView) findViewById(R.id.cardResult);
        imgLoad = (ImageView) findViewById(R.id.imgLoad);
        txtName = (TextView) findViewById(R.id.txtName);
        img = getIntent().getStringExtra("IMG");
        Log.d("img", img);
        //    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        // imgCaptured.setImageBitmap(bitmap);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Picasso.with(FullScreenImg.this)
                .load(new File(img)).resize(width, height)
                .centerCrop()
                .into(imgCaptured, new Callback() {
                    @Override
                    public void onSuccess() {
                        imgCaptured.setDrawingCacheEnabled(true);
                        imgCaptured.buildDrawingCache();
                        bmap = imgCaptured.getDrawingCache();
                        setAnimation();
                    }

                    @Override
                    public void onError() {
                        Log.d("error", "error");
                    }
                });


    }

    private void setAnimation() {
        if (imgScan.getVisibility() == View.GONE) {
            imgScan.setVisibility(View.VISIBLE);


            imgScan.setVisibility(View.VISIBLE);
            mAnimation = new TranslateAnimation(
                    TranslateAnimation.ABSOLUTE, 0f,
                    TranslateAnimation.ABSOLUTE, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);
            mAnimation.setDuration(2000);
            mAnimation.setRepeatCount(-1);
            mAnimation.setRepeatMode(Animation.INFINITE);
            mAnimation.setInterpolator(new LinearInterpolator());
            imgScan.setAnimation(mAnimation);
            //    postData();

            uplaod();

            //   imgScan.animate().setDuration(3500).translationY(0).;
        }
    }

    public void uplaod() {
        requestQueue = Volley.newRequestQueue(FullScreenImg.this);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, image_requests, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    String token = result.getString("token");

                    getImageResponse(token);

                    Log.d("Response", result.toString());


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse.statusCode == 404) {
                    //errorMessage = "Resource not found";
                } else if (networkResponse.statusCode == 401) {
                    // errorMessage = message+" Please login again";
                } else if (networkResponse.statusCode == 400) {
                    //   errorMessage = message+ " Check your inputs";
                } else if (networkResponse.statusCode == 500) {
                    //  errorMessage = message+" Something is getting wrong";
                } else if (networkResponse.statusCode == 504) {
                    uplaod();
                }
                error.printStackTrace();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "CloudSight " + API_KEY);
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image_request[locale]", "en-US");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                params.put("image_request[image]", new DataPart("file_avatar.jpg", byteArray, "image/jpeg"));
                //   params.put("cover", new DataPart("file_cover.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mCoverImage.getDrawable()), "image/jpeg"));

                return params;
            }
        };
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(multipartRequest);
    }

    private void getImageResponse(final String token) {
        Log.d("site", image_responses + token);
        JsonObjectRequest jsonData = new JsonObjectRequest(Request.Method.GET,
                image_responses + token, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject res) {

                        Log.d("Data", res.toString());
                        try {
                            status = res.getString("status");
                            if (status.equals("not completed") || status.equals("dark")) {
                                getImageResponse(token);
                            } else {
                                Log.d("Data", res.toString());
                                parseJSON(res);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //    parseJSON(response);

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Data", error.toString());
                //pd.dismiss();
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "CloudSight " + API_KEY);
                return params;
            }
        };
        requestQueue.add(jsonData);
    }

    private void parseJSON(JSONObject res) {

        if (cardResult.getVisibility() == View.GONE) {
            try {
                mAnimation.cancel();
                imgScan.setVisibility(View.GONE);

                cardResult.setVisibility(View.VISIBLE);
                Picasso.with(FullScreenImg.this).load(new File(img)).resize(100, 100)
                        .centerCrop().into(imgLoad);
                txtName.setText(res.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
                txtName.setText("Error");
            }

        }

    }

}
