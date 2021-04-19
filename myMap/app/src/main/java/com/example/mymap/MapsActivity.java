package com.example.mymap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.LocationListener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //設定超過一個時間 毫秒 他就會做更新location 的動作
//    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 3000;
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; //10s
    private static final int DEFAULT_CAMERA_ZOOM = 16;
    private float DEFAULT_CHANGE_THRESHOLD = 0.0f;
    //    private static final int DEFAULT_SHAKE_THRESHOLD = 16;
    private static final int DEFAULT_SHAKE_THRESHOLD = -9;
    private static final int SENSOR_FREEZE_TIME = 3000;

    //    private static final long SENSOR_DELAY = 200000;
    private static final int SENSOR_DELAY = 10000000;
    private static final int THRESHOLD_SAMPLE = 50;
    private static final int TIME_THRESHOLD = 5000;

    private boolean islocationPermission = false;
    private boolean isFirst = true;
//    private boolean isTest = true;
    private boolean isTest = false;

    private static final String TAG = "HW1";
    protected LocationManager locationManager;
    private Location lastKnownLocation;
    protected TextView statusTextView;
    protected Button startButton;
    protected Button stopButton;
    private ArrayList<LatLng> trackLocations;
    private ArrayList<LatLng> shakeLocations;
    private String provider = null;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private List<Double> sensorValues;
    private Date tmpDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        setContentView(R.layout.activity_maps);
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
        statusTextView = (TextView) findViewById(R.id.location);
        startButton = (Button) findViewById(R.id.startbtn);
        stopButton = (Button) findViewById(R.id.stopbtn);

        startButton.setOnClickListener(startClickListener);
        stopButton.setOnClickListener(stopClickListener);
        getGPSPermission();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }//end of onCreate

    @Override
    protected void onResume() {
        super.onResume();
        // sensorManager.registerListener(accelerometerLister, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // sensorManager.unregisterListener(accelerometerLister);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady");

        mMap = googleMap;
        Log.i(TAG, "getCurrentLocation");
        if (mMap == null) {
            Log.i(TAG, "Cannot find my Map");
            return;
        }
        initMap();

    }//end of onMapReady

    private void initMap() {
        getLocationPermission();
        if (islocationPermission) {
            setProvider();
            initLocationUI();
            initLocation();
//            registerLocationListener();
        }
    }

    private void getLocationPermission() {
        Log.i(TAG, "getLocationPermission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Cannot get the permission of location", Toast.LENGTH_SHORT).show();
            islocationPermission = false;
        }
        islocationPermission = true;
        Toast.makeText(this, "Get location permission.", Toast.LENGTH_SHORT).show();
    }

    private void setProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (islocationPermission) {
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
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                Toast.makeText(this, "Use GPS location", Toast.LENGTH_SHORT).show();
                provider = LocationManager.GPS_PROVIDER;
            } else {
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
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location == null) {
                    Toast.makeText(this, "Network location is Null, break", Toast.LENGTH_SHORT).show();
                    provider = null;
                } else {
                    Toast.makeText(this, "Use network", Toast.LENGTH_SHORT).show();
                    provider = LocationManager.NETWORK_PROVIDER;
                }
            }
        } else {
            Toast.makeText(this, "Cannot set provider", Toast.LENGTH_SHORT).show();
        }
        if (isTest)
            provider = LocationManager.NETWORK_PROVIDER;
    }

    public void initLocation() {

        // set NYCU as start position
        // LatLng currLocation = new LatLng(24.7869954, 120.9974482);
        // set near location for test
        LatLng baseLocation = new LatLng(24.7869954, 120.9974482);
        if (isTest) {
            baseLocation = new LatLng(25.010764927473524, 121.45534402918176);
        }
        LatLng currLocation;

        addMark(baseLocation, "base position", BitmapDescriptorFactory.HUE_RED);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (provider != null) {
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
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null)
            {
                currLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.i(TAG, "currLocation, latitude: " + Double.toString(location.getLatitude()) +
                        ", longitude: " + Double.toString(location.getLongitude()));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_CAMERA_ZOOM));
            }
        }

    }

    public void getGPSPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                99);
        return;
    }

    public void registerLocationListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    99);
            return;
        }
        Log.i(TAG, "provider:" + provider);
//        Log.i(TAG, "provider" + provider);
//        Log.i(TAG, "provider" + provider);
        locationManager.requestLocationUpdates(provider, MINIMUM_TIME_BETWEEN_UPDATES, DEFAULT_CHANGE_THRESHOLD, locationListener);
    }

    public void initLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (islocationPermission) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void addMark(LatLng currLocation, String mark_title, float color) {
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(currLocation);
        // markerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOpt.icon(BitmapDescriptorFactory.defaultMarker(color));
        if (trackLocations != null)
        {
            if (trackLocations.size() == 1)
            {
                mark_title = "Initial Position";
            }
            else
            {
                mark_title = "Current Position " + (trackLocations.size()-1);
            }
        }

        markerOpt.title(mark_title);
        mMap.addMarker(markerOpt).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLocation));
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void cameraFocus(double lat, double lng){
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(DEFAULT_CAMERA_ZOOM)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }

    private void trackToLocation(double lat, double lng){
        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : trackLocations) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.RED);

        Polyline line = mMap.addPolyline(polylineOpt);
        line.setWidth(10);
    }

    private double random_position(double pos)
    {
        if (isTest)
        {
            int x_random = new Random().nextInt(21)-10;
            Log.i(TAG, "x_random:" + x_random);

            pos += x_random * 0.001;
        }
        return pos;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged");
            if (trackLocations != null)
            {
                Toast.makeText(MapsActivity.this, "get location into list", Toast.LENGTH_SHORT).show();
                double lng = location.getLongitude();
                double lat = location.getLatitude();

                if (isFirst)
                {
                    isFirst = false;
                }
                else
                {
                    lat = random_position(lat);
                    lng = random_position(lng);

                }

                LatLng this_location = new LatLng(lat, lng);
                if (trackLocations == null) {
                    trackLocations = new ArrayList<LatLng>();
                }
                trackLocations.add(this_location);

                addMark(this_location, "", BitmapDescriptorFactory.HUE_RED);
                cameraFocus(lat, lng);
                trackToLocation(lat, lng);

            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged");
        }

    };

    Button.OnClickListener startClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "start Click");
//            sensorManager.registerListener(accelerometerLister, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(accelerometerLister, accelerometerSensor, SENSOR_DELAY);

            registerLocationListener();
            trackLocations = new ArrayList<LatLng>();
            shakeLocations = new ArrayList<LatLng>();
            Toast.makeText(MapsActivity.this, "Start to track", Toast.LENGTH_SHORT).show();
        }
    };
    Button.OnClickListener stopClickListener = new Button.OnClickListener(){
        public void onClick(View v) {
            Log.i(TAG,"stop Click");
            sensorManager.unregisterListener(accelerometerLister);

            locationManager.removeUpdates(locationListener);
            trackLocations = null;
            shakeLocations = null;
            Toast.makeText(MapsActivity.this, "Stop to track", Toast.LENGTH_SHORT).show();
        }
    };

    private void addShakeMark(LatLng currLocation, String mark_title, float color) {
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(currLocation);
        markerOpt.icon(BitmapDescriptorFactory.defaultMarker(color));
        if (shakeLocations != null)
        {
            if (shakeLocations.size() > 0)
            {
                mark_title = "Shake Position" + shakeLocations.size();
            }
        }
        markerOpt.title(mark_title);
        mMap.addMarker(markerOpt).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLocation));
    }


    public SensorEventListener accelerometerLister = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            statusTextView.setText("Y axis \t\t" + y);
            Log.i(TAG, "y:" + y);

            if (y < DEFAULT_SHAKE_THRESHOLD )
            {
                if (tmpDate == null)
                {
                    tmpDate = new Date();
                }

                if (new Date().getTime() - tmpDate.getTime() > TIME_THRESHOLD)
                {
                    sensorValues = null;
                }

                if (sensorValues == null)
                {
                    sensorValues = new ArrayList<Double>();
                    tmpDate = new Date();
                }
                sensorValues.add((double) y);
                Log.i(TAG, "sensorValues.size(): "+ sensorValues.size());
                Log.i(TAG, "Duration: "+ (new Date().getTime() - tmpDate.getTime()));

                if (sensorValues.size() > THRESHOLD_SAMPLE && (new Date().getTime() - tmpDate.getTime() < TIME_THRESHOLD))
                {
                    sensorValues = null;
                    Log.i(TAG, "Shake Event!!!");
                    Log.i(TAG, "y" + y);
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (trackLocations != null) {
                        double lng = location.getLongitude();
                        double lat = location.getLatitude();

                        lat = random_position(lat);
                        lng = random_position(lng);
                        LatLng this_location = new LatLng(lat, lng);

                        trackLocations.add(this_location);
                        shakeLocations.add(this_location);
                        addShakeMark(this_location, "", BitmapDescriptorFactory.HUE_YELLOW);
                        cameraFocus(lat, lng);
                        trackToLocation(lat, lng);

                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


}//end of class MapsActivity
