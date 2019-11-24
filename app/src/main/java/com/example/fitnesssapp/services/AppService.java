package com.example.fitnesssapp.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.androdocs.httprequest.HttpRequest;
import com.example.fitnesssapp.AppController;
import com.example.fitnesssapp.HomeActivity;
import com.example.fitnesssapp.Locations.APIClient;
import com.example.fitnesssapp.Locations.GoogleMapAPI;
import com.example.fitnesssapp.Locations.PlacesResult;
import com.example.fitnesssapp.Locations.Result;
import com.example.fitnesssapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppService extends Service {

    Context context;
    private LocationManager locationmanager = null;
    FirebaseAuth auth;
    FirebaseFirestore db;
    private String TAG = "================Fitness================";
    String uid;
    HomeActivity homeActivity;
    AppController appController;
    private int totalStepsFromDataPoints, currentSteps,initialSteps ;
    int defaultSteps = 1200;

    private Handler mHandler = new Handler();


    private Runnable init = new Runnable() {
        @Override
        public void run() {

           // Toast.makeText(context, "OK it works", Toast.LENGTH_SHORT).show();
            //TODO : change the time  (FOR TESTING PURPOSE ONLY)
            mHandler.postDelayed(this, 300000 );
        }
    };

    public void onCreate() {
        // The service is being created

    }
    public AppService() {

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = auth.getCurrentUser().getUid();
        homeActivity = new HomeActivity();
        appController = new AppController();

    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        this.context = this;

       initializeLocationManager();

        init.run();

        return START_STICKY;
    }





    @SuppressLint("MissingPermission")
    private void initializeLocationManager() {
        if (locationmanager == null){
            locationmanager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 1000, 1, locationListener);

        }
    }
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            String key = getText(R.string.google_maps_key).toString();
            String lat = String.valueOf(location.getLatitude());
            String lon = String.valueOf(location.getLongitude());
            appController.setLatitude(lat);
            appController.setLongitude(lon);

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
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
