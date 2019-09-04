package com.andromeda.healthcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class PrescriptionActivity extends AppCompatActivity {

    Button addPrescription;
    TextView medicineText;

    FirebaseDatabase database;
    TinyDB tinyDB;
    String email, medicine, rule, comment, prescriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        addPrescription = findViewById(R.id.add_prescription_button);
        medicineText = findViewById(R.id.medicine_text);

        database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();

        tinyDB = new TinyDB(getApplicationContext());
        email = tinyDB.getString("email");
        medicine = tinyDB.getString("medicine");
        rule = tinyDB.getString("rule");
        comment = tinyDB.getString("comment");

        prescriptionText = medicine + "\n" + rule + "\n" + comment;
        medicineText.setText(prescriptionText);

        if(isValidEmail(email)) {
            String path = email.substring(0, email.indexOf('@'));
            myRef.child(path).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("prescription")) {
                        medicine = Objects.requireNonNull(dataSnapshot.child("prescription").child("medicine").getValue()).toString();
                        rule = Objects.requireNonNull(dataSnapshot.child("prescription").child("rule").getValue()).toString();
                        comment = Objects.requireNonNull(dataSnapshot.child("prescription").child("comment").getValue()).toString();

                        prescriptionText = medicine + "\n" + rule + "\n" + comment;
                        medicineText.setText(prescriptionText);

                        tinyDB.putString("medicine", medicine);
                        tinyDB.putString("rule", rule);
                        tinyDB.putString("comment", comment);
                    } else {
                        Toast.makeText(getApplicationContext(), "No prescription found", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        addPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrescriptionActivity.this, DigitalPrescriptionActivity.class);
                startActivity(intent);
            }
        });
    }

    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
