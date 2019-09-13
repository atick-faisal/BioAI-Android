package com.andromeda.healthcare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class RequesterActivity extends AppCompatActivity implements OnMapReadyCallback {

    String contact, location;
    String[] lat_long;
    boolean mapReady = false;

    MapView mapView;
    GoogleMap googleMap;

    TextView recipientName, recipientBlood, recipientContact;
    Button callButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requester);
        ////////////////////////////////////////////////////////////////////////////////////////////
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        recipientName = findViewById(R.id.recipient_name);
        recipientBlood = findViewById(R.id.recipient_blood);
        recipientContact = findViewById(R.id.recipient_contact);
        callButton = findViewById(R.id.call_recipient_button);
        Intent intent = getIntent();
        recipientName.setText(intent.getStringExtra("name"));
        recipientBlood.setText(intent.getStringExtra("blood_group"));
        contact = intent.getStringExtra("contact");
        location = intent.getStringExtra("location");
        ////////////////////////////////////////////////////////////////////////////////////////////
        if (location != null) {
            lat_long = location.split(",");
        }
        recipientContact.setText(contact);
        ////////////////////////////////////////////////////////////////////////////////////////////
        callButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + contact));
                startActivity(intent);
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMapReady(GoogleMap g) {
        googleMap = g;
        googleMap.setMinZoomPreference(15);
        googleMap.setBuildingsEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        LatLng current_location = new LatLng(Double.parseDouble(lat_long[0]), Double.parseDouble(lat_long[1]));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(current_location);
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
        mapReady = true;
    }
}
