package com.andromeda.healthcare;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class AssistantActivity extends AppCompatActivity {

    MqttAndroidClient client;
    TinyDB tinyDB;

    String serverIP = "";
    String serverURL = "tcp://broker.hivemq.com:1883";
    String publishTopic = "questions";
    String subscribeTopic = "diagnosis";
    String text;

    boolean connectionFlag = false;
    private static final int REQ_CODE_SPEECH_INPUT = 1;

    TextView diagnosisText, inputText;
    EditText brokerIPField, questionField;
    Button connectButton, searchButton;
    ImageView speakButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);
        ////////////////////////////////////////////////////////////////////////////////////////////
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        diagnosisText = findViewById(R.id.diagnosis_text);
        inputText = findViewById(R.id.input_text);
        speakButton = findViewById(R.id.speak_button);
        brokerIPField = findViewById(R.id.broker_id);
        connectButton = findViewById(R.id.conncet_button);
        questionField = findViewById(R.id.question_text_field);
        searchButton = findViewById(R.id.diagnosis_search_button);
        ////////////////////////////////////////////////////////////////////////////////////////////
        Toast.makeText(getApplicationContext(), "The recommendations of an open source AI is not a substitute for professional medical care", Toast.LENGTH_LONG).show();
        ////////////////////////////////////////////////////////////////////////////////////////////
        tinyDB = new TinyDB(getApplicationContext());
        if (!tinyDB.getString("serverIP").matches("")) {
            serverIP = tinyDB.getString("serverIP");
            brokerIPField.setText(serverIP);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (brokerIPField.getText() != null) {
                    serverIP = brokerIPField.getText().toString();
                    tinyDB.putString("serverIP", serverIP);
                    serverURL = "tcp://" + brokerIPField.getText().toString() + ":1883";
                    connectToBroker();
                }
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionField.getText() != null) {
                    text = questionField.getText().toString();
                    inputText.setVisibility(View.VISIBLE);
                    inputText.setText(text);
                    sendMessage(publishTopic, text);
                }
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAudioInput();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void getAudioInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your situation. Please mention age and symptoms");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException ignored) {

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
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
                    sendMessage(publishTopic, text);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
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
                    Toast.makeText(getApplicationContext(), "Connected to AI service...", Toast.LENGTH_SHORT).show();
                    connectionFlag = true;
                    subscribeToTopic(subscribeTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed to connect...", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Loading Diagnosis...", Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void subscribeToTopic(String topic) {
        try {
            if (client.isConnected()) {
                client.subscribe(topic, 0);
                Toast.makeText(getApplicationContext(), "Subscribed...", Toast.LENGTH_SHORT).show();
                client.setCallback(new MqttCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void connectionLost(Throwable cause) {
                        Toast.makeText(getApplicationContext(), "Connection Lost...", Toast.LENGTH_SHORT).show();
                        connectionFlag = false;
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
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
