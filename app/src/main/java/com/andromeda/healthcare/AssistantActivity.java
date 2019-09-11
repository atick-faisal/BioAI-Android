package com.andromeda.healthcare;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class AssistantActivity extends AppCompatActivity {

    MqttAndroidClient client;

    String serverIP = "";
    String serverURL = "tcp://broker.hivemq.com:1883";
    String topic = "questions";
    String sTopic = "diagnosis";

    boolean connectionFlag = false;

    private TextView diagnosisText, inputText;
    EditText brokerIPField, questionField;
    Button connectButton, searchButton;

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

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        diagnosisText = findViewById(R.id.diagnosis_text);
        inputText = findViewById(R.id.input_text);
        ImageView speakButton = findViewById(R.id.speak_button);
        brokerIPField = findViewById(R.id.broker_id);
        connectButton = findViewById(R.id.conncet_button);
        questionField = findViewById(R.id.question_text_field);
        searchButton = findViewById(R.id.diagnosis_search_button);



//        LayoutInflater inflater = getLayoutInflater();
//        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
//        Toast toast = new Toast(getApplicationContext());
//        toast.setDuration(Toast.LENGTH_LONG);
//        toast.setView(layout);
//        toast.show();

        Toast.makeText(getApplicationContext(), "The recommendations of an open source AI is not a substitute for professional medical care", Toast.LENGTH_LONG).show();


        tinyDB = new TinyDB(getApplicationContext());
        if(!tinyDB.getString("serverIP").matches("")) {
            serverIP = tinyDB.getString("serverIP");
            brokerIPField.setText(serverIP);
        }

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(brokerIPField.getText() != null) {
                    serverIP = brokerIPField.getText().toString();
                    tinyDB.putString("serverIP", serverIP);
                    serverURL = "tcp://" + brokerIPField.getText().toString() + ":1883";
                    connectToBroker();
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(questionField.getText() != null) {
                    text = questionField.getText().toString();
                    inputText.setVisibility(View.VISIBLE);
                    inputText.setText(text);
                    sendMessage(topic, text);
                }
            }
        });

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAudioInput();
            }
        });

        //connectToBroker();
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
                    inputText.setVisibility(View.VISIBLE);
                    inputText.setText(text);
                    sendMessage(topic, text);
                }

                for (int i = 0; i < 200; i++) {
                    if (text.contains(Database.SYMPTOM_NAME[i].toLowerCase())) {
                        symptomIDs.append(Database.SYMPTOM_ID[i]).append(",");
                    }
                }
                Toast.makeText(getApplicationContext(), "Loading Diagnosis", Toast.LENGTH_SHORT).show();
                //FetchDiagnosis fetchDiagnosis = new FetchDiagnosis();
                //fetchDiagnosis.execute();
            }
        }
    }

    void connectToBroker() {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), serverURL, clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    brokerIPField.setVisibility(View.GONE);
                    connectButton.setVisibility(View.GONE);
                    questionField.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                    connectionFlag = true;
                    subscribeToTopic(sTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    void sendMessage(String topic, String msg) {
        byte[] encodedPayload;
        try {
            encodedPayload = msg.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            Toast.makeText(getApplicationContext(), "Sent", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void subscribeToTopic(String topic) {
        try {
            if (client.isConnected()) {
                client.subscribe(topic, 0);
                Toast.makeText(getApplicationContext(), "Subscribed", Toast.LENGTH_SHORT).show();
                client.setCallback(new MqttCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void connectionLost(Throwable cause) {
                        Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_SHORT).show();
                        connectionFlag = false;
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        diagnosisText.setVisibility(View.VISIBLE);
                        diagnosisText.setText(message.toString());
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionFlag) {
            try {
                IMqttToken disconnectToken = client.disconnect();
                disconnectToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        finish();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
            connectionFlag = false;
        }
    }

}
