package com.example.fitnesssapp.Locations;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.fitnesssapp.HomeActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.fitnesssapp.R;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LocationsActivity extends AppCompatActivity implements LocationListener {


    private static final int FINE_LOCATION_REQUEST_CODE = 101 ;
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private ListView listview;
    private static final String API_KEY = "AIzaSyCzaZKM2YSOlxBaRGRvzQVkbElMfivcgZc";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String TYPE_SEARCH = "/nearbysearch";
    private static final String OUT_JSON = "/json?";
    private static final String LOG_TAG = "ListRest";
    private View mLayout;
    public Criteria criteria;
    public String bestProvider;
    LocationManager lm;
    public double longitude,latitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION_REQUEST_CODE);
        else {
            Log.d("TAG", "Fine Location permission already granted");

        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mLayout = findViewById(R.id.layout);
        toolbar.setTitle(getString(R.string.title_activity_locations));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LocationsActivity.this, HomeActivity.class));
                finish();
            }
        });


        showRestaurants();




    }

    private void showRestaurants() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, show restaurants
            Snackbar.make(mLayout,
                    "Location permission available. Show restaurants.",
                    Snackbar.LENGTH_SHORT).show();
            startRestaurants();
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission();
        }
    }

    private void startRestaurants() {



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            criteria = new Criteria();
            bestProvider = String.valueOf(lm.getBestProvider(criteria, true));


            int radius = 1000;

            if(location != null){

                longitude = location.getLongitude();
             latitude = location.getLatitude();

             ArrayList<Place> list = search(longitude, latitude, radius);

            if (list != null) {
                listview = (ListView) findViewById(R.id.listView);
                ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, list);
                listview.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No Gyms nearby. Sorry", Toast.LENGTH_SHORT).show();
            }
            }
            else{
                lm.requestLocationUpdates(bestProvider,1000,0,  this);
            }







        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(this);

    }

    @Override
    public void onLocationChanged(Location location) {
        //Hey, a non null location! Sweet!

        //remove location callback:
        lm.removeUpdates(this);

        //open the map:
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(LocationsActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(mLayout, "Location access is required to display restaurants near you.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(LocationsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION);
                }
            }).show();

        } else {
            Snackbar.make(mLayout,
                    "Permission is not available. Requesting location permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }




    private ArrayList<Place> search(double latitude, double longitude, int radius) {

        ArrayList<Place> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_SEARCH);
            sb.append(OUT_JSON);
            sb.append("location=" + String.valueOf(latitude) + "," + String.valueOf(longitude));
            sb.append("&radius=" + String.valueOf(radius));
            sb.append("&type=restaurant");
            sb.append("&key=" + API_KEY);

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }




        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("results");

            // Extract the descriptions from the results
            resultList = new ArrayList<Place>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                Place place = new Place();
                place.reference = predsJsonArray.getJSONObject(i).getString("reference");
                place.name = predsJsonArray.getJSONObject(i).getString("name");
                resultList.add(place);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }

        return resultList;
    }

    //Value Object for the ArrayList
    public static class Place {
        private String reference;
        private String name;

        public Place(){
            super();
        }
        @Override
        public String toString(){
            return this.name; //This is what returns the name of each restaurant for array list
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for location permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start preview Activity.
                Snackbar.make(mLayout, "Location permission granted. Showing restaurants.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                startRestaurants();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Location permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }



}
