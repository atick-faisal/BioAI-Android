package com.andromeda.healthcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button assistantButton, profileButton, emergencyButton, bloodBankButton, prescriptionButton, myPrescriptionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assistantButton = findViewById(R.id.assistant_button);
        profileButton = findViewById(R.id.profile_button);
        emergencyButton = findViewById(R.id.emergency_button);
        bloodBankButton = findViewById(R.id.blood_bank_button);
        prescriptionButton = findViewById(R.id.prescription_button);
        myPrescriptionButton = findViewById(R.id.my_prescription_button);

        assistantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AssistantActivity.class);
                startActivity(intent);
            }
        });
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EmergencyActivity.class);
                startActivity(intent);
            }
        });
        bloodBankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BloodActivity.class);
                startActivity(intent);
            }
        });
        prescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DigitalPrescriptionActivity.class);
                startActivity(intent);
            }
        });
        myPrescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PrescriptionActivity.class);
                startActivity(intent);
            }
        });
    }
}
