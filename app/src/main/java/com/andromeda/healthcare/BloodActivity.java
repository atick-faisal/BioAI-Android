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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class BloodActivity extends AppCompatActivity {

    EditText searchField;
    Button searchButton;

    String name, email, path, location, contact;

    FirebaseDatabase database;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood);
        ////////////////////////////////////////////////////////////////////////////////////////////
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        searchField = findViewById(R.id.search_field);
        searchButton = findViewById(R.id.search_button);
        ////////////////////////////////////////////////////////////////////////////////////////////
        tinyDB = new TinyDB(getApplicationContext());
        name = tinyDB.getString("name");
        email = tinyDB.getString("email");
        location = tinyDB.getString("location");
        contact = tinyDB.getString("phone");
        ////////////////////////////////////////////////////////////////////////////////////////////
        if (isValidEmail(email)) {
            path = email.substring(0, email.indexOf('@'));
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Your request is pending...", Toast.LENGTH_LONG).show();
                final String blood_group = searchField.getText().toString();
                if (blood_group.matches("")) {
                    Toast.makeText(getApplicationContext(), "Field empty", Toast.LENGTH_SHORT).show();
                } else {
                    database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();

                    myRef.orderByChild("blood_group").equalTo(blood_group).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                DatabaseReference reference = snapshot.getRef();
                                if (!Objects.requireNonNull(reference.getKey()).matches(path)) {
                                    reference.child("request").child("status").setValue("pending");
                                    reference.child("request").child("blood_group").setValue(blood_group);
                                    reference.child("request").child("name").setValue(name);
                                    reference.child("request").child("location").setValue(location);
                                    reference.child("request").child("contact").setValue(contact);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
