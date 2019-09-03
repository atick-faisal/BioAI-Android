package com.andromeda.healthcare;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class AssistantActivity extends AppCompatActivity {

    private TextView diagnosisText, inputText;

    public FirebaseDatabase database = FirebaseDatabase.getInstance();
    public TinyDB tinyDB;
    private static final int REQ_CODE_SPEECH_INPUT = 1;
    public String API_URL = "https://healthservice.priaid.ch/symptoms?token=" + Credentials.API_KEY + "&format=json&language=en-gb";
    public String text = "I have chronic abdominal pain".toLowerCase();
    public StringBuilder symptomIDs = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        diagnosisText = findViewById(R.id.diagnosis_text);
        inputText = findViewById(R.id.input_text);
        Button speakButton = findViewById(R.id.speak_button);

        tinyDB = new TinyDB(getApplicationContext());

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAudioInput();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchDiagnosis extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String update = "";
            try {
                API_URL = "https://healthservice.priaid.ch/diagnosis?symptoms=[" + symptomIDs + "]&gender=male&year_of_birth=1996&token=" + Credentials.API_KEY + "&format=json&language=en-gb";
                URL url = new URL(API_URL);
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    update = stringBuilder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return update;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONArray jsonArray = new JSONArray(s);
                String diagnosis = jsonArray.getJSONObject(0).getJSONObject("Issue").getString("IcdName");
                String displayText = "You Might Have \n" + diagnosis;
                diagnosisText.setText(displayText);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getAudioInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException ignored) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null) {
                    text = result.get(0);
                }
                inputText.setText(text);
                for (int i = 0; i < 200; i++) {
                    if (text.contains(Database.SYMPTOM_NAME[i].toLowerCase())) {
                        symptomIDs.append(Database.SYMPTOM_ID[i]).append(",");
                    }
                }
                Toast.makeText(getApplicationContext(), "Loading Diagnosis", Toast.LENGTH_SHORT).show();
                FetchDiagnosis fetchDiagnosis = new FetchDiagnosis();
                fetchDiagnosis.execute();
            }
        }
    }

}
