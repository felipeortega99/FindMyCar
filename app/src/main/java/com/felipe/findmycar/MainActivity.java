package com.felipe.findmycar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton main_fab, fab_tracking, fab_gps, fab_return_tracking;
    Animation FabOpen, FabClose, FabRClockwise, FabRAnticlockwise;
    Boolean isOpen = false;
    Boolean isTrackingOn = false;
    Boolean isGpsTrackingOn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab_return_tracking = (FloatingActionButton) findViewById(R.id.fab_return);
        fab_tracking = (FloatingActionButton) findViewById(R.id.fab_tracking);
        fab_gps = (FloatingActionButton) findViewById(R.id.fab_gps);
        main_fab = (FloatingActionButton) findViewById(R.id.main_fab);
        //Animations
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        FabRClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        FabRAnticlockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anticlockwise);

        main_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen) {
                    fab_gps.startAnimation(FabClose);
                    fab_tracking.startAnimation(FabClose);
                    main_fab.startAnimation(FabRClockwise);
                    main_fab.setImageResource(R.drawable.ic_action_arrow_drop_up);
                    fab_gps.setClickable(false);
                    fab_tracking.setClickable(false);
                    isOpen = false;

                } else {
                    fab_gps.startAnimation(FabOpen);
                    fab_tracking.startAnimation(FabOpen);
                    main_fab.startAnimation(FabRAnticlockwise);
                    main_fab.setImageResource(R.drawable.ic_action_close);
                    fab_gps.setClickable(true);
                    fab_tracking.setClickable(true);
                    isOpen = true;
                }
            }


        });

        fab_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_return_tracking.setImageResource(R.drawable.ic_action_return_tracking);
                main_fab.setEnabled(false);
                fab_return_tracking.setClickable(true);
            }
        });

        fab_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_return_tracking.setImageResource(R.drawable.ic_action_return_tracking_gps);
                main_fab.setEnabled(false);
                fab_return_tracking.setClickable(true);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // mapFragment.
    }
}
