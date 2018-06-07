package com.felipe.findmycar;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    public FloatingActionButton main_fab, fab_tracking, fab_gps, fab_return_tracking;
    public TextView txt_distance, txt_duration;
    public CardView cardView;
    public Animation FabOpen, FabClose, FabRClockwise, FabRAnticlockwise;
    public Boolean isOpen = false;
    public Boolean isGps = false;
    public Boolean isTracking = false;
    public TextToSpeech tts;
    //Maps
    private GoogleMap mGoogleMap;
    private double latitude;
    private double longitude;
    public MarkerOptions marcadorDestino, marcadorOrigen;
    //GPS
    protected Context context;
    protected LocationManager locationManager;
    android.location.Location location;
    boolean statusLocationListener;
    // The minimum distance to change updates
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //meters
    // The minimum time between updates
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //CardView
        cardView = (CardView) findViewById(R.id.cardView);
        //TextViews
        txt_distance = (TextView) findViewById(R.id.txt_distance);
        txt_duration = (TextView) findViewById(R.id.txt_duration);
        //Fab buttons
        fab_return_tracking = (FloatingActionButton) findViewById(R.id.fab_return);
        fab_tracking = (FloatingActionButton) findViewById(R.id.fab_tracking);
        fab_gps = (FloatingActionButton) findViewById(R.id.fab_gps);
        main_fab = (FloatingActionButton) findViewById(R.id.main_fab);
        //Animations
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        FabRClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        FabRAnticlockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anticlockwise);

        //GPS --->
        locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Log.e("Location", "latitude:" + location.getLatitude() + ", longitude:" + location.getLongitude());
        statusLocationListener = true;
        //----->

        // mapFragment.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onTrackingClick(View view) {
        closeFabButton();
        fab_return_tracking.setImageResource(R.drawable.ic_action_simple_marker);
        main_fab.setBackgroundColor(Color.parseColor("#343434"));
        main_fab.setEnabled(false);
        fab_return_tracking.setClickable(true);
        fab_return_tracking.setVisibility(View.VISIBLE);
        isTracking = true;
    }

    public void onTrackingGpsClick(View view) {
        closeFabButton();
        fab_return_tracking.setImageResource(R.drawable.ic_action_car_white);
        main_fab.setBackgroundColor(Color.parseColor("#343434"));
        main_fab.setEnabled(false);
        fab_return_tracking.setClickable(true);
        fab_return_tracking.setVisibility(View.VISIBLE);
        isGps = true;
    }

    public void onMainFabButtonClick(View view) {
        if (isOpen) {
            closeFabButton();

        } else {
            openFabButton();
        }
    }

    public void onReturnTrackingClick(View view) {
        if (isGps) {
            addMarker();
            //String url = obtenerDireccionesURL(marcadorOrigen.getPosition(), marcadorDestino.getPosition());
            LatLng test = new LatLng(31.9005858, -116.6950596);
            String url = obtenerDireccionesURL(test, marcadorDestino.getPosition());
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);

            main_fab.setBackgroundColor(Color.parseColor("#DC552C"));
            main_fab.setEnabled(true);
            fab_return_tracking.setImageResource(R.drawable.ic_action_return_tracking);
            cardView.setVisibility(View.VISIBLE);
            isGps = false;
        } else if (isTracking) {

        } else {
            mGoogleMap.clear();
            cardView.setVisibility(View.INVISIBLE);
            fab_return_tracking.setVisibility(View.INVISIBLE);
        }
    }

    private void addMarker() {
        marcadorOrigen = new MarkerOptions();
        //LatLng origen = new LatLng(latitude, longitude);
        LatLng origen = new LatLng(31.9005858, -116.6950596);
        marcadorOrigen.position(origen);
        marcadorOrigen.title("Aquì estoy");
        mGoogleMap.addMarker(marcadorOrigen);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origen, 19));
    }

    public void closeFabButton() {
        fab_gps.startAnimation(FabClose);
        fab_tracking.startAnimation(FabClose);
        main_fab.setImageResource(R.drawable.ic_action_arrow_drop_up);
        fab_gps.setClickable(false);
        fab_tracking.setClickable(false);
        isOpen = false;
    }

    public void openFabButton() {
        fab_gps.startAnimation(FabOpen);
        fab_tracking.startAnimation(FabOpen);
        main_fab.startAnimation(FabRAnticlockwise);
        main_fab.setImageResource(R.drawable.ic_action_close);
        fab_gps.setClickable(true);
        fab_tracking.setClickable(true);
        isOpen = true;
    }

    public void speak(String word){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        //LatLng myLocation = new LatLng(latitude, longitude);
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

        marcadorDestino = new MarkerOptions();
        marcadorDestino.position(myLocation);
        marcadorDestino.title("Aquì estoy estacionado");
        marcadorDestino.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_car_marker));
        mGoogleMap.addMarker(marcadorDestino);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    private String obtenerDireccionesURL(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        Log.e("URL", "" + url);
        return url;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("OBTENER INFO DEL WS", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

            ParserTaskDirections parserTaskDirections = new ParserTaskDirections();
            parserTaskDirections.execute(result);
        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creamos una conexion http
                urlConnection = (HttpURLConnection) url.openConnection();

                // Conectamos
                urlConnection.connect();

                // Leemos desde URL
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.rgb(0, 0, 255));
            }
            if (lineOptions != null) {
                mGoogleMap.addPolyline(lineOptions);
            }
        }
    }

    private class ParserTaskDirections extends AsyncTask<String, Integer, HashMap<String, String>> {

        @Override
        protected HashMap<String, String> doInBackground(String... jsonData) {

            JSONObject jObject;
            HashMap<String, String> directionsList = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                directionsList = parser.getDuration(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return directionsList;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> result) {
            String duration, distance;

            duration = result.get("duration");
            distance = result.get("distance");

            txt_duration.setText(duration);
            txt_distance.setText("(" + distance + ")");
        }
    }
}
