package com.andromeda.healthcare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    EditText nameField, emailField, bloodField, ageField;
    RadioGroup genderSelector;
    RadioButton radioButton;
    Button submitButton;

    String name, email, blood_group, age, gender;

    TinyDB tinyDB;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameField = findViewById(R.id.name_field);
        emailField = findViewById(R.id.email_field);
        bloodField = findViewById(R.id.blood_field);
        ageField = findViewById(R.id.age_field);
        genderSelector = findViewById(R.id.gender_selector);
        submitButton = findViewById(R.id.submit_button);

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

                if(name.matches("") || email.matches("") || blood_group.matches("") || age.matches("")) {
                    Toast.makeText(getApplicationContext(), "Empty Field", Toast.LENGTH_SHORT).show();
                } else {
                    tinyDB.putString("name", name);
                    tinyDB.putString("email", email);
                    tinyDB.putString("blood_group", blood_group);
                    tinyDB.putString("age", age);

                    database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();

                    if(isValidEmail(email)) {
                        String path = email.substring(0, email.indexOf('@'));
                        myRef.child(path).child("name").setValue(name);
                        myRef.child(path).child("blood_group").setValue(blood_group);
                        myRef.child(path).child("age").setValue(age);
                        myRef.child(path).child("gender").setValue(gender);
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

            gender = tinyDB.getString("gender");

            if(gender.equals("Male")) {
                radioButton = findViewById(R.id.male_button);
                radioButton.setChecked(true);
            } else {
                radioButton = findViewById(R.id.female_button);
                radioButton.setChecked(true);
            }
        } finally {

        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
