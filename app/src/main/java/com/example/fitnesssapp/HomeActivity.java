package com.example.fitnesssapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;


import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fitnesssapp.*;
import com.example.fitnesssapp.Authentication.ProfileActivity;
import com.example.fitnesssapp.Authentication.LoginActivity;
import com.example.fitnesssapp.Locations.LocationsActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener{


    private static final int FINE_LOCATION_REQUEST_CODE = 100;
    private static final String TAG = "Fitness";
    private static final int OAUTH_REQUEST_CODE = 101 ;
    private static final int CLIENT_API_REQUEST_CODE = 303;
    GoogleApiClient mClient;
    private FirebaseAuth auth;
    SharedPreferences sharedPreferences;
    TextView helloText;
    ArcProgress stepsCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_home));
        setSupportActionBar(toolbar);
        //Firebase auth instance
        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        helloText = (TextView)headerView.findViewById(R.id.helloTextView);
        stepsCounter = (ArcProgress)findViewById(R.id.arc_progress);
        String name =sharedPreferences.getString("FirstName",null);
        helloText.setText("Hello there "+ name+" !");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION_REQUEST_CODE);
        else{
            Log.d(TAG, "Fine Location permission already granted");
            mClient.connect();
        }
        //Is the User logged in already?
        if (auth.getCurrentUser() == null) {

            //then reedirect to home page
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        // Get token
        // [START retrieve_current_token]
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                    }
                });
        // [END retrieve_current_token]

        // [START fcm_runtime_enable_auto_init]
      //  FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        // [END fcm_runtime_enable_auto_init]

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();
        if (id == R.id.nav_profile){
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
            finish();
        }
       else if (id == R.id.nav_locations){
            startActivity(new Intent(HomeActivity.this, LocationsActivity.class));
            finish();
        }
       else if (id == R.id.nav_about){
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
            finish();
        }
        else if (id == R.id.nav_awards){
            startActivity(new Intent(HomeActivity.this, AchievementsActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google API connected");

        if(hasOAuthPermission()) {
            Log.d(TAG, "OAuth Permissions already granted");

            readDataFromHistoryApi();
            listAvailableDatSources();
            listHistorySubscription();
        }
        else {
            Log.e(TAG, "OAuth Permission not granted.");
            requestOAuthPermission();
        }
    }

    private void requestOAuthPermission() {
        Log.d(TAG, "Attempting OAuth 2.0");

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build();
        GoogleSignIn.requestPermissions(this,OAUTH_REQUEST_CODE,GoogleSignIn.getLastSignedInAccount(this),fitnessOptions);
    }

    private boolean hasOAuthPermission() {
        Log.d(TAG, "Checking for OAuth permission");

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build();
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this),fitnessOptions);
    }


    private void readDataFromHistoryApi(){
        Log.d(TAG, "Accessing data from History API");

        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
//        cal.add(Calendar.DATE, -4);
        long startTime = cal.getTimeInMillis();



        final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
//                .bucketByActivityType(1, TimeUnit.MILLISECONDS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        PendingResult<DataReadResult> pendingResult = Fitness.HistoryApi.readData(mClient,dataReadRequest);
        pendingResult.setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult dataReadResult) {
                Log.d(TAG, "History API data received");
                List<Bucket> bucket = dataReadResult.getBuckets();
                if(bucket.size() == 1){
                    Log.d(TAG, "Single bucket data retrieved");
                    displayBucketData(bucket);
                    return;
                }
                else if(bucket.size() > 1){
                    Log.d(TAG, "Multiple bucket data retrieved");
                    logBucketData(bucket);
                    return;
                }
                Log.e(TAG, "Unexpected bucket size");
            }
        });
    }

    private void displayBucketData(List<Bucket> bucketList) {
        Log.d(TAG, "Displaying Bucket data");
        for(Bucket bucket : bucketList){
//            String info = "Bucket Type "+String.valueOf(bucket.getBucketType())
//                    + " Activity Type: " + bucket.getActivity();
            String info = "StartTime: "+String.valueOf(bucket.getStartTime(TimeUnit.MILLISECONDS))+" EndTime: "+String.valueOf(bucket.getEndTime(TimeUnit.MILLISECONDS));
            List<DataPoint> dp = bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA).getDataPoints();
            for(DataPoint dataPoint : dp) {
                String stepCount = String.valueOf(dataPoint.getValue(Field.FIELD_STEPS));
                stepsCounter.setProgress(Integer.parseInt(stepCount));

               // stepCountTextView.setText(stepCount);
                info += " Aggregate Steps: " + stepCount;
            }
            Log.i(TAG, info);

        }
    }



    private void logBucketData(List<Bucket> bucketList) {
        Log.d(TAG, "Displaying Bucket data");
        for(Bucket bucket : bucketList){
//            String info = "Bucket Type "+String.valueOf(bucket.getBucketType())
//                    + " Activity Type: " + bucket.getActivity();
            String info = "StartTime: "+String.valueOf(bucket.getStartTime(TimeUnit.MILLISECONDS))
                    +" EndTime: "+String.valueOf(bucket.getEndTime(TimeUnit.MILLISECONDS));
            List<DataPoint> dp = bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA).getDataPoints();
            for(DataPoint dataPoint : dp) {
                String stepCount = String.valueOf(dataPoint.getValue(Field.FIELD_STEPS));
                info += " Aggregate Steps: " + stepCount;
            }
            Log.i(TAG, info);

        }
    }

    private void listAvailableDatSources() {
        Log.d(TAG, "Fetching fitness data source");
        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .findDataSources(new DataSourcesRequest.Builder()
                        .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                        .setDataSourceTypes(DataSource.TYPE_RAW)
                        .build())
                .addOnSuccessListener(new OnSuccessListener<List<DataSource>>() {
                    @Override
                    public void onSuccess(List<DataSource> dataSources) {
                        Log.d(TAG, "Data source fetched successfully. Find data list below.");
                        Log.i(TAG, dataSources.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"failed", e);
                    }
                });
    }

    public void listHistorySubscription(){
        Log.d(TAG, "Fetching List of history Subscription");

        PendingResult<ListSubscriptionsResult> pendingResult =Fitness.RecordingApi.listSubscriptions(mClient);
        pendingResult.setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
            @Override
            public void onResult(@NonNull ListSubscriptionsResult listSubscriptionsResult) {
                Log.d(TAG, "Recording Subscription List fetched successfully");
                Log.i(TAG, listSubscriptionsResult.toString());
                if(listSubscriptionsResult.getSubscriptions().size() < 1){
                    Log.d(TAG, "Subscription List empty.");
                    createRecordingSubscription();
                }

            }
        });
    }

    private void createRecordingSubscription() {
        Log.d(TAG, "Creating A recording subscription");

        PendingResult<Status> pendingResult = Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_STEP_COUNT_DELTA);
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.d(TAG, "Recording subscription created successfully.");
                Log.i(TAG, status.getStatus().toString());
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if(connectionResult.getErrorCode() == FitnessStatusCodes.SIGN_IN_REQUIRED) {
            Log.d(TAG, "Client API connection failed. Attempting resolution if possible");
            Log.e(TAG, connectionResult.toString());
            try {
                connectionResult.startResolutionForResult(HomeActivity.this, CLIENT_API_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case FINE_LOCATION_REQUEST_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Fine location permission granted");
                    mClient.connect();
                }
                else    finish();
            }
            break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CLIENT_API_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                Log.d(TAG, "Api client connection successful");
                mClient.connect();
            }
        }
        else if(requestCode == OAUTH_REQUEST_CODE){
            if(resultCode == RESULT_OK) {
                Log.d(TAG, "OAuth request complete. Fitness permission authorised");

                readDataFromHistoryApi();
//                listAvailableDatSources();
                listHistorySubscription();
            }
        }
    }
}
