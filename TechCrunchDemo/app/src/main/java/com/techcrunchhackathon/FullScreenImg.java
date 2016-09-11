package com.techcrunchhackathon;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.techcrunchhackathon.utils.AppHelper;
import com.techcrunchhackathon.utils.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FullScreenImg extends AppCompatActivity {
    private static final String WORKPACE_ID = "0d6a382d-dbf4-4a82-a984-7f0cd54a764b";
    private static final String USER_NAME = "0d6eb480-83b0-4fce-a588-f492f0d6432e";
    private static final String PASSWORD = "LTTiuZiZaL7M";
    private static final String CON_USER_NAME = "da8ba92d-db2c-4cc3-8a59-115ba249ccc3";
    private static final String CON_PASSWORD = "C7KrV3WpDIuG";
    private static final String T2S_USERNAME = "2b3a7bd8-9ded-433d-9bb9-e2ddce37f7d6";
    private static final String T2S_PASSWORD = "8cXO4oAsnp3v";
    int sampleRate = 16000;
    private static final String API_KEY = "NWhnKnq8GTrx1zT4yV3_sQ";
    private ImageView imgScan, imgLoad;
    private TranslateAnimation mAnimation;
    private StreamPlayer player = new StreamPlayer();
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
    private FloatingActionButton fabVoice;
    private SpeechToText speechService;
    private LanguageTranslation translationService;
    private TextView txtOutPut;
    private String outpputText;
    private List<Transcript> speechResultses;
    private Toolbar toolbar;
    private ConversationService conService;
    private MessageResponse response;
    private MessageRequest newMessage;
    private String talkText;
    private TextToSpeech textService;
    private File file;
    private String intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_img);
        imgScan = (ImageView) findViewById(R.id.imgScan);
        imgCaptured = (ImageView) findViewById(R.id.imgCaptured);
        cardResult = (CardView) findViewById(R.id.cardResult);
        txtOutPut = (TextView) findViewById(R.id.txtOutPut);
        fabVoice = (FloatingActionButton) findViewById(R.id.fabVoice);
        imgLoad = (ImageView) findViewById(R.id.imgLoad);
        txtName = (TextView) findViewById(R.id.txtName);
        img = getIntent().getStringExtra("IMG");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Scanning....");
        speechService = initSpeechToTextService();
        textService = initTextToSpeechService();
        translationService = initLanguageTranslationService();
        initConversationService();
        file = new File(Environment.getExternalStorageDirectory() + "/play.wav");
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

        fabVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fabVoice.setBackgroundTintList(ColorStateList.valueOf(Color
                                            .parseColor("#00796B")));
                                }
                            });
                            RecognizeOptions options = new RecognizeOptions.Builder()
                                    .continuous(true)
                                    .interimResults(true)
                                    .timestamps(true)
                                    .wordConfidence(true)

                                    .inactivityTimeout(5) // use this to stop listening when the speaker pauses, i.e. for 5s
                                    .contentType(HttpMediaType.AUDIO_RAW + "; rate=" + sampleRate)
                                    .build();

                            speechService.recognizeUsingWebSocket(new MicrophoneInputStream(), options, new BaseRecognizeCallback() {
                                @Override
                                public void onTranscription(SpeechResults speechResults) {
                                    super.onTranscription(speechResults);
                                    speechResultses = speechResults.getResults();

                                    String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                                    System.out.println(text);
                                    showMicText(text);
                                }
                            });

                        } catch (Exception e) {
                            showError(e);
                        }
                    }
                }).start();
            }
        });
    }

    private LanguageTranslation initLanguageTranslationService() {
        LanguageTranslation service = new LanguageTranslation();
        String username = "0d6eb480-83b0-4fce-a588-f492f0d6432e";
        String password = "LTTiuZiZaL7M";
        service.setUsernameAndPassword(username, password);
        return service;
    }


//    private class MicrophoneRecognizeDelegate implements RecognizeDelegate {
//
//        @Override
//        public void onMessage(SpeechResults speechResults) {
//            String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
//            showMicText(text);
//        }
//
//        @Override
//        public void onConnected() {
//
//        }
//
//        @Override
//        public void onError(Exception e) {
//            showError(e);
//            //      enableMicButton();
//        }
//
//        @Override
//        public void onDisconnected() {
//            //  enableMicButton();
//        }
//    }

    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Toast.makeText(FullScreenImg.this, text, Toast.LENGTH_LONG).show();
                outpputText = text;
                txtOutPut.setText(text);
                Log.d("Text", text);
                sendConverstation(text);
            }
        });
    }

    private void sendConverstation(String text) {
        new SendConversation().execute(text, "", "");

    }

    class SendConversation extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String txt = strings[0];
            newMessage = new MessageRequest.Builder().inputText(txt).build();
            response = conService.message(WORKPACE_ID, newMessage).execute();
            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("ok")) {
                Log.d("RES", response.toString());
                parseConvResponse(response.toString());

            }
            super.onPostExecute(s);
        }
    }

    private void parseConvResponse(String s) {

        try {
            JSONObject json = new JSONObject(s);
            talkText = json.getJSONObject("output").getJSONArray("text").get(0).toString();
            intent = json.getJSONArray("intents").getJSONObject(0).getString("intent");
            Log.d("Talk", talkText);
            Log.d("intent", intent);
            //
            showIntents();

            new SynthesisTask().execute(talkText, "", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showIntents() {
        if (intent.equals("hello disrupt")) {

        } else if (intent.equals("product")) {
            Intent in = new Intent(FullScreenImg.this, ShowProducts.class);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
        } else if (intent.equals("judges")) {
            Intent in = new Intent(FullScreenImg.this, ShowJudgesActivity.class);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
        } else if (intent.equals(" Show recent posts")) {

        }
    }

    private class SynthesisTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {

                InputStream stream = textService.synthesize(params[0], Voice.EN_ALLISON, AudioFormat.WAV).execute();
                player.playStream(stream);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("ok")) {

            }
        }
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FullScreenImg.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
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
                imgCaptured.setVisibility(View.GONE);
                cardResult.setVisibility(View.VISIBLE);
                fabVoice.setVisibility(View.VISIBLE);
                toolbar.setTitle("Context");
                if (txtOutPut.getVisibility() == View.GONE) {
                    txtOutPut.setVisibility(View.VISIBLE);
                }

                Picasso.with(FullScreenImg.this).load(new File(img)).resize(100, 100)
                        .centerCrop().into(imgLoad);
                txtName.setText(res.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
                txtName.setText("Error");
            }

        }

    }

    private void initConversationService() {
        conService = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
        conService.setUsernameAndPassword(CON_USER_NAME, CON_PASSWORD);

    }

    private SpeechToText initSpeechToTextService() {
        SpeechToText service = new SpeechToText();
        String username = USER_NAME;
        String password = PASSWORD;
        service.setUsernameAndPassword(username, password);
        service.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
        return service;
    }

    private TextToSpeech initTextToSpeechService() {
        TextToSpeech service = new TextToSpeech();
        String username = T2S_USERNAME;
        String password = T2S_PASSWORD;
        service.setUsernameAndPassword(username, password);
        return service;
    }
}
