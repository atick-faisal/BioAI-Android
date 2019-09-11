package com.andromeda.healthcare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    TinyDB tinyDB;
    String name, email, blood_group, location, contact;
    String pendingRequest = "canceled";
    public DatabaseReference pendingReference;

    LinearLayout assistantButton, profileButton, emergencyButton, bloodBankButton, prescriptionButton, myPrescriptionButton, pendingRequestField;
    Button acceptButton, cancelButton;
    TextView bloodReuestText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        assistantButton = findViewById(R.id.assistant_button);
        profileButton = findViewById(R.id.profile_button);
        emergencyButton = findViewById(R.id.emergency_button);
        bloodBankButton = findViewById(R.id.blood_bank_button);
        prescriptionButton = findViewById(R.id.prescription_button);
        myPrescriptionButton = findViewById(R.id.my_prescription_button);
        pendingRequestField = findViewById(R.id.blood_request_notifier);
        acceptButton = findViewById(R.id.accept_button);
        cancelButton = findViewById(R.id.cancel_button);
        bloodReuestText = findViewById(R.id.blood_request_text);


        tinyDB = new TinyDB(getApplicationContext());
        email = tinyDB.getString("email");

        database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pendingReference.child("request").child("status").setValue("canceled");
                pendingRequestField.setVisibility(View.GONE);
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RequesterActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("location", location);
                intent.putExtra("blood_group", blood_group);
                intent.putExtra("contact", contact);
                startActivity(intent);
            }
        });

        if(isValidEmail(email)) {
            String path = email.substring(0, email.indexOf('@'));
            myRef.child(path).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("request")) {
                        pendingRequest = Objects.requireNonNull(dataSnapshot.child("request").child("status").getValue()).toString();
                        if(pendingRequest.matches("pending")) {
                            pendingRequestField.setVisibility(View.VISIBLE);
                            pendingReference = dataSnapshot.getRef();
                            name = Objects.requireNonNull(dataSnapshot.child("request").child("name").getValue()).toString();
                            blood_group = Objects.requireNonNull(dataSnapshot.child("request").child("blood_group").getValue()).toString();
                            location = Objects.requireNonNull(dataSnapshot.child("request").child("location").getValue()).toString();
                            contact = Objects.requireNonNull(dataSnapshot.child("request").child("contact").getValue()).toString();
                            String pendingText = name + " is Looking for " + blood_group + " Blood";
                            bloodReuestText.setText(pendingText);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

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

        ////////////////////////////////////////////////////////////////////////////////////////////

        requestPermissions();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.INTERNET}, 0);
            }

            else if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 0);
            }

            else if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 0);
            }

            else if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }

            else  if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
            } else {
                Toast.makeText(getApplicationContext(), "Please allow permissions", Toast.LENGTH_LONG).show();
                requestPermissions();
            }
        }
    }

    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
