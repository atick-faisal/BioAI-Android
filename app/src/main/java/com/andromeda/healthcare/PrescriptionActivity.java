package com.andromeda.healthcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

public class PrescriptionActivity extends AppCompatActivity {

    Button addPrescription;
    TextView medicineText;
    Switch reminderSwitch;

    final Calendar calendar = Calendar.getInstance();
    FirebaseDatabase database;
    TinyDB tinyDB;
    String email, medicine, rule, comment, prescriptionText;
    int[] dayTime = new int[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //addPrescription = findViewById(R.id.add_prescription_button);
        medicineText = findViewById(R.id.medicine_text);
        reminderSwitch = findViewById(R.id.reminder_switch);

        database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();

        tinyDB = new TinyDB(getApplicationContext());
        email = tinyDB.getString("email");
        medicine = tinyDB.getString("medicine");
        rule = tinyDB.getString("rule");
        comment = tinyDB.getString("comment");

        if(tinyDB.getBoolean("reminder_key")) {
            reminderSwitch.setChecked(true);
        } else {
            if(tinyDB.getBoolean("reminder_key")) {
                reminderSwitch.setChecked(false);
            }
        }

        if(!rule.matches("")) {
            setTimeParams(rule);
        }

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

                        if(!rule.matches("")) {
                            setTimeParams(rule);
                        }

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

        reminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                tinyDB.putBoolean("reminder_key", b);
                if(compoundButton.isChecked()) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    for(int i = 0; i < 3; i++) {
                        if(dayTime[i] == 1) {
                            setCalendarTime(i);
                            Intent intent = new Intent(PrescriptionActivity.this, medicine_notifier.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(PrescriptionActivity.this, 1, intent, 0);
                            if (alarmManager != null) {
                                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                        AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
                            }
                        }
                    }
                } else {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(PrescriptionActivity.this, medicine_notifier.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(PrescriptionActivity.this, 1, intent, 0);
                    if (alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                    }
                }
            }
        });

//        addPrescription.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(PrescriptionActivity.this, DigitalPrescriptionActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    private void setCalendarTime(int timeOfDay) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        switch (timeOfDay) {
            case 0:
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                break;
            case 1:
                calendar.set(Calendar.HOUR_OF_DAY, 13);
                break;
            case 2:
                calendar.set(Calendar.HOUR_OF_DAY, 22);
                break;
            default:
                break;
        }
    }

    private void setTimeParams(String medicine_rule) {
        String[] rules = medicine_rule.split("-");
        for(int i = 0; i < 3; i++) {
            dayTime[i] = Integer.parseInt(rules[i]);
        }
    }

    private static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
