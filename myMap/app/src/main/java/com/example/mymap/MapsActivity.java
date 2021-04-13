package com.example.mymap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //設定超過一個時間 毫秒 他就會做更新location 的動作
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; //10s

    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }//end of onCreate

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
        mMap = googleMap;

        // Add a marker in NYCU and move the camera
        LatLng NYCU = new LatLng(24.7869954, 120.9974482);
        mMap.addMarker(new MarkerOptions().position(NYCU).title("工程三館"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(NYCU));

        LatLng currLocation = NYCU;
        LatLng lastLocation = NYCU;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        while (currLocation != null)   //or while 1
        {
            //sleep 10s , then get next location
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            lastLocation = currLocation;
            currLocation = null;

            //get location
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
            //得知GPS 位置時，在Text view上顯示經緯度
            if(location == null)
            {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location == null) {
                    Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            MarkerOptions markerOpt = new MarkerOptions();
            currLocation = new LatLng(location.getLatitude(),location.getLongitude());
            markerOpt.position(currLocation);
            //markerOpt.title("current location");
            markerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(markerOpt).showInfoWindow();

            PolylineOptions polylineOpt = new PolylineOptions();
            polylineOpt.add(currLocation);
            polylineOpt.add(lastLocation);
            polylineOpt.color(Color.BLUE);//線條顏色
            Polyline polyline = mMap.addPolyline(polylineOpt);
            polyline.setWidth(5); //線條寬度
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation)); //yui add
        }
    }//end of onMapReady
}//end of class MapsActivity