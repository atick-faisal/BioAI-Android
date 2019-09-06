package com.andromeda.healthcare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class EmergencyActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText contactField;
    Button addContact, sendButton, callButton;
    TextView contactList, locationText;

    TinyDB tinyDB;
    FusedLocationProviderClient mFusedLocationClient;
    String geocode;
    Location location;

    String number;
    boolean mapReady = false;

    MapView mapView;
    GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        contactField = findViewById(R.id.contact_field);
        addContact = findViewById(R.id.add_contact_button);
        contactList = findViewById(R.id.contacts_list);
        sendButton = findViewById(R.id.send_button);
        callButton = findViewById(R.id.call_button);
        locationText = findViewById(R.id.location_text);

        mapView = findViewById(R.id.current_location_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EmergencyActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 1);

                return;
            }
        }

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tinyDB = new TinyDB(getApplicationContext());
                number = contactField.getText().toString();
                if (number.length() != 11) {
                    Toast.makeText(getApplicationContext(), "Number is incorrect", Toast.LENGTH_SHORT).show();
                } else {
                    contactList.setText(number);
                    number = "+88" + number;
                    tinyDB.putString("number", number);
                }
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:999"));
                startActivity(intent);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSms();
            }
        });

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
                            location = l;
                            if(mapReady) {
                                LatLng current_location = new LatLng(location.getLatitude(), location.getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(current_location);
                                googleMap.addMarker(markerOptions);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
                            }
                            geocode = l.getLatitude() + "," + l.getLongitude();
                            locationText.setText(geocode);
                        } else {
                            Toast.makeText(getApplicationContext(), "Current location unavailable", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        __init__();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onMapReady(GoogleMap gmap) {
        googleMap = gmap;
        googleMap.setMinZoomPreference(15);
        googleMap.setBuildingsEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        if(location != null) {
            LatLng current_location = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(current_location);
            googleMap.addMarker(markerOptions);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
        }
        mapReady = true;
    }

    private void __init__()  {
        tinyDB = new TinyDB(getApplicationContext());
        number = tinyDB.getString("number");
        if(!number.matches("")) {
            contactList.setText(number.substring(3));
        } else {
            contactList.setText("No Contacts Added");
        }
    }

    public void sendSms() {
        String smsBody = "Emergency! Help Me ! I am Here : https://www.google.com/maps/?q=" + geocode;
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, smsBody, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(EmergencyActivity.this, "Permission denied to make calls", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
