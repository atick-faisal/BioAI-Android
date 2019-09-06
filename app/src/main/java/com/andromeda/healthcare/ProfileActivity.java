package com.andromeda.healthcare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    EditText nameField, emailField, bloodField, ageField, phoneField;
    RadioGroup genderSelector;
    RadioButton radioButton;
    Button submitButton;

    String name, email, blood_group, age, gender, phone;

    TinyDB tinyDB;
    FirebaseDatabase database;
    FusedLocationProviderClient mFusedLocationClient;
    String geocode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        nameField = findViewById(R.id.name_field);
        emailField = findViewById(R.id.email_field);
        bloodField = findViewById(R.id.blood_field);
        ageField = findViewById(R.id.age_field);
        genderSelector = findViewById(R.id.gender_selector);
        phoneField = findViewById(R.id.phone_number_field);
        submitButton = findViewById(R.id.submit_button);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Location l) {
                        if (l != null) {
                            geocode = l.getLatitude() + "," + l.getLongitude();
                        } else {
                            Toast.makeText(getApplicationContext(), "Current location unavailable", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedID = genderSelector.getCheckedRadioButtonId();
                radioButton = findViewById(selectedID);
                gender = radioButton.getText().toString();

                tinyDB = new TinyDB(getApplicationContext());
                name = nameField.getText().toString();
                email = emailField.getText().toString();
                blood_group = bloodField.getText().toString();
                age = ageField.getText().toString();
                phone = phoneField.getText().toString();

                if(name.matches("") || email.matches("") || blood_group.matches("") || age.matches("") || phone.matches("")) {
                    Toast.makeText(getApplicationContext(), "Empty Field", Toast.LENGTH_SHORT).show();
                } else {
                    tinyDB.putString("name", name);
                    tinyDB.putString("email", email);
                    tinyDB.putString("blood_group", blood_group);
                    tinyDB.putString("age", age);
                    tinyDB.putString("gender", gender);
                    tinyDB.putString("phone", phone);

                    database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();

//                    myRef.orderByChild("blood_group").equalTo("B+").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
//                                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
//                                submitButton.setText(name);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });

                    if(isValidEmail(email)) {
                        String path = email.substring(0, email.indexOf('@'));
                        myRef.child(path).child("name").setValue(name);
                        myRef.child(path).child("blood_group").setValue(blood_group);
                        myRef.child(path).child("age").setValue(age);
                        myRef.child(path).child("gender").setValue(gender);
                        myRef.child(path).child("phone").setValue(phone);
                        myRef.child(path).child("location").setValue(geocode);
                    } else {
                        Toast.makeText(getApplicationContext(), "Email not valid", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        __init__();
    }

    private void __init__()  {
        tinyDB = new TinyDB(getApplicationContext());
        try {
            name = tinyDB.getString("name");
            nameField.setText(name);
            email = tinyDB.getString("email");
            emailField.setText(email);
            blood_group = tinyDB.getString("blood_group");
            bloodField.setText(blood_group);
            age = tinyDB.getString("age");
            ageField.setText(age);
            phone = tinyDB.getString("phone");
            phoneField.setText(phone);

            gender = tinyDB.getString("gender");

            if(gender.matches("Male")) {
                radioButton = findViewById(R.id.male_button);
                radioButton.setChecked(true);
                radioButton = findViewById(R.id.female_button);
                radioButton.setChecked(false);
            } else {
                radioButton = findViewById(R.id.female_button);
                radioButton.setChecked(true);
                radioButton = findViewById(R.id.male_button);
                radioButton.setChecked(false);
            }
        } finally {

        }
    }

    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
