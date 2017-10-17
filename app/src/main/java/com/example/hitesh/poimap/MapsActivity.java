package com.example.hitesh.poimap;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    protected GoogleMap mMap, mMap2;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    protected Location lastlocation;
    private Marker currentLocation;

    public static Location Mylocation;

    public static final int REQUEST_LOCATION_CODE = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.imageButton);

        fab.setImageResource(R.drawable.ic_add_black_24dp);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            chechLocationPermission();

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (client == null) {
                            buildGoogleClient();
                        } else {
                            Toast.makeText(this, "plese allow permission", Toast.LENGTH_SHORT).show();
                        }
                    }
                }


        }


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
        mMap = googleMap;
        mMap2 = googleMap;

        // Add a marker in Sydney and move the camera
      /*  LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //
            //             int[] grantResults)
            buildGoogleClient();
            mMap.setMyLocationEnabled(true);

            if (mMap2 != null) {
                //setUpMap();
                new MarkerTask(mMap2).execute();
            }

            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }




    }

    public void toAddPoi(View view) {
        Intent i = new Intent(this, AddPoi.class);
        startActivity(i);

    }



    protected synchronized void buildGoogleClient() {

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();

    }


    @Override
    public void onLocationChanged(Location location) {

        lastlocation = location;
        Mylocation = location;

        if (currentLocation != null) {

            currentLocation.remove();

        }

        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

        Log.e("location", latlng.toString());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title("current location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        currentLocation = mMap.addMarker(markerOptions);



        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);

        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

        }


    }

    public boolean chechLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);


            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);

            }

            return false;
        } else
            return true;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}


class MarkerTask extends AsyncTask<Void, Void, String> {

    private static final String LOG_TAG = "ExampleApp";

    private static final String SERVICE_URL = "http://dev.citrans.net:8888/skymeet/poi/list";

    // Invoked by execute() method of this object
    GoogleMap mMap;

    public MarkerTask(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    protected String doInBackground(Void... args) {

        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            // Connect to the web service
            URL url = new URL(SERVICE_URL);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Read the JSON data into the StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to service", e);
            //throw new IOException("Error connecting to service", e); //uncaught
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return json.toString();
    }

    // Executed after the complete execution of doInBackground() method
    @Override
    protected void onPostExecute(String json) {

        try {
            // De-serialize the JSON string into an array of city objects
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);

                LatLng latLng = new LatLng(jsonObj.getJSONArray("location").getDouble(0),
                        jsonObj.getJSONArray("location").getDouble(1));

                Log.e("abc", " " + latLng);

                //move CameraPosition on first result
                if (i == 0) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng).zoom(10).build();

                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                }

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.markandroid);
                // Bitmap b=icon.getBitmap();
                //  Bitmap smallMarker = Bitmap.createScaledBitmap(icon, 80, 80, false);
                // Create a marker for each city in the JSON data.

                String status = "ACTIVE";


                if (jsonObj.getString("status").equals(status)) {
                    mMap.addMarker(new MarkerOptions()
                            .icon(icon)
                            .title(jsonObj.getString("title"))
                            .position(latLng));

                }

            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON", e);
        }

    }
}

