package com.andromeda.healthcare;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class DigitalPrescriptionActivity extends AppCompatActivity {

    EditText emailField, medicineField, commentField;
    Button morningButton, noonButton, nightButton, doneButton;

    int morning = 0, noon = 0, night = 0;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_prescription);
        ////////////////////////////////////////////////////////////////////////////////////////////
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        emailField = findViewById(R.id.patient_email_field);
        medicineField = findViewById(R.id.medicine_name_field);
        commentField = findViewById(R.id.comment_field);
        morningButton = findViewById(R.id.morning_button);
        noonButton = findViewById(R.id.noon_button);
        nightButton = findViewById(R.id.night_button);
        doneButton = findViewById(R.id.done_button);
        ////////////////////////////////////////////////////////////////////////////////////////////
        morningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (morning == 0) {
                    morning = 1;
                    morningButton.setBackground(getResources().getDrawable(R.drawable.indigo_circle));
                    morningButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    morning = 0;
                    morningButton.setBackground(getResources().getDrawable(R.drawable.ash_circle));
                    morningButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                morningButton.setText(String.format(Locale.US, "%d", morning));
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////
        noonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noon == 0) {
                    noon = 1;
                    noonButton.setBackground(getResources().getDrawable(R.drawable.indigo_circle));
                    noonButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    noon = 0;
                    noonButton.setBackground(getResources().getDrawable(R.drawable.ash_circle));
                    noonButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                noonButton.setText(String.format(Locale.US, "%d", noon));
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////
        nightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (night == 0) {
                    night = 1;
                    nightButton.setBackground(getResources().getDrawable(R.drawable.indigo_circle));
                    nightButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    night = 0;
                    nightButton.setBackground(getResources().getDrawable(R.drawable.ash_circle));
                    nightButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                nightButton.setText(String.format(Locale.US, "%d", night));
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString();
                if (isValidEmail(email)) {
                    String comment = commentField.getText().toString();
                    String medicine = medicineField.getText().toString();
                    String path = email.substring(0, email.indexOf('@'));
                    database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();
                    myRef.child(path).child("prescription").child("medicine").setValue(medicine);
                    myRef.child(path).child("prescription").child("comment").setValue(comment);
                    myRef.child(path).child("prescription").child("rule").setValue(String.format(Locale.US, "%d-%d-%d", morning, noon, night), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(getApplicationContext(), "Failed to connect to database...", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Sent to patient...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Email not valid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
