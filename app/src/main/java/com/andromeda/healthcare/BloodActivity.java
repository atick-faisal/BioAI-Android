package com.andromeda.healthcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class BloodActivity extends AppCompatActivity {

    EditText searchField;
    Button searchButton;
    TextView searchResult;
    ListView resultList;

    String[] resultArray;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood);

        searchField = findViewById(R.id.search_field);
        searchButton = findViewById(R.id.search_button);
        searchResult = findViewById(R.id.search_result);
        resultList = findViewById(R.id.result_list);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String blood_group = searchField.getText().toString();
                if(blood_group.matches("")) {
                    Toast.makeText(getApplicationContext(), "Field empty", Toast.LENGTH_SHORT).show();
                } else {
                    database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();

                    myRef.orderByChild("blood_group").equalTo(blood_group).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int i = 0;
                            resultArray = new String[(int) dataSnapshot.getChildrenCount()];
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                resultArray[i++] = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            }
                            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, resultArray);
                            resultList.setAdapter(adapter);
                            if(i == 0) {
                                Toast.makeText(getApplicationContext(), "No result found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }
}
