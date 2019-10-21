package com.example.fitnesssapp;

import android.Manifest;
import android.app.Activity;
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
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    GoogleApiClient mClient;
    private String TAG = "Fitness";
    private final int OAUTH_REQUEST_CODE = 200;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private final int FINE_LOCATION_REQUEST_CODE = 101;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private static OnDataPointListener stepListener;
    private static OnDataPointListener distanceListener;

    SharedPreferences sharedPreferences;
    TextView helloText, stepsPercentage, dateTextView, calories, distance, activeTime;
    ArcProgress stepsCounter;
    private float distanceInMeters;
    private float kCals;
    private float movemins;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_home));
        setSupportActionBar(toolbar);


        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        String uid = auth.getCurrentUser().getUid();



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        //Display Name of the user in the navigation header
        helloText = (TextView)headerView.findViewById(R.id.helloTextView);
        stepsPercentage = (TextView)findViewById(R.id.stepsPercent);
        dateTextView = (TextView)findViewById(R.id.todayDate);
        calories = findViewById(R.id.caloriesTextview);
        distance = findViewById(R.id.distanceTextview);
        activeTime = findViewById(R.id.activetimeTextview);


        //Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM , yyyy");
        String currentDateandTime = sdf.format(new Date());
        dateTextView.setText(currentDateandTime);



        stepsCounter = (ArcProgress)findViewById(R.id.arc_progress);

        //set the steps counter maximum value to goal set
        stepsCounter.setMax(sharedPreferences.getInt("Goal",5000));


//        String name = sharedPreferences.getString("FirstName",null);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION_REQUEST_CODE);
        else {
            Log.d(TAG, "Fine Location permission already granted");

        }


        //Is the User logged in already?
        if (auth.getCurrentUser() == null) {

            //then reedirect to home page
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }


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




            FitnessOptions fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES,FitnessOptions.ACCESS_READ)
                    .build();

        // check if app has permissions
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount( this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount( this),
                    fitnessOptions);
        } else {
            accessGoogleFit();
        }

        retrieveUserDetails(uid);
    }

    private void accessGoogleFit() {


        // Subscribe to recordings
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount((AppCompatActivity) this))
                .subscribe(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });


        Fitness.getRecordingClient((AppCompatActivity) this, GoogleSignIn.getLastSignedInAccount((AppCompatActivity) this))
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });
        Fitness.getRecordingClient((AppCompatActivity) this, GoogleSignIn.getLastSignedInAccount((AppCompatActivity) this))
                .subscribe(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });
        Fitness.getRecordingClient((AppCompatActivity) this, GoogleSignIn.getLastSignedInAccount((AppCompatActivity) this))
                .subscribe(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });

        Fitness.getRecordingClient((AppCompatActivity) this, GoogleSignIn.getLastSignedInAccount((AppCompatActivity) this))
                .subscribe(DataType.TYPE_MOVE_MINUTES)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });
        // end of subscriptions



        // prepare to get history

        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        long startTime = cal.getTimeInMillis();

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        Log.d(TAG,"Range Start: " + dateFormat.format(startTime));
        Log.d(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED,DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_MOVE_MINUTES,DataType.AGGREGATE_MOVE_MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        // get history
        Fitness.getHistoryClient((AppCompatActivity) this, GoogleSignIn.getLastSignedInAccount((AppCompatActivity) this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d(TAG, "successfully got history");
                        getDataSetsFromBucket(dataReadResponse);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "failed to get history", e);
                    }
                });


    }

    private void getDataSetsFromBucket(DataReadResponse dataReadResponse) {
        // number of buckets would always be 1 (bucket size was set to 365 days in readRequest)
        if (dataReadResponse.getBuckets().size() > 0) {
            Log.d(TAG, "Number of returned buckets of DataSets is: " + dataReadResponse.getBuckets().size());
            for (Bucket bucket : dataReadResponse.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    parseDataSet(dataSet);
                }
            }
        }
    }

    private void parseDataSet(DataSet dataSet) {
        Log.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();

        int totalStepsFromDataPoints = 0;
        float distanceTraveledFromDataPoints = 0;
        float kcals = 0;
        long mins = 0;

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.d(TAG, "Data point:");
            Log.d(TAG, "\tType: " + dp.getDataType().getName());
            Log.d(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.d(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));



            mins = dp.getEndTime(TimeUnit.MINUTES) - dp.getStartTime(TimeUnit.MINUTES) ;
            movemins += mins;
            activeTime.setText(String.format("%.2f", movemins/1000.00));


            for (Field field : dp.getDataType().getFields()) {
                Log.d(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));

                // increment the steps or distance
                if (field.getName().equals("steps")) {
                    totalStepsFromDataPoints += dp.getValue(field).asInt();

                } else if (field.getName().equals("distance")) {
                    distanceTraveledFromDataPoints += dp.getValue(field).asFloat();
                }else if (field.getName().equals("calories")) {
                    kcals += dp.getValue(field).asFloat();
                }




            }
        }

        //////////////////// update the proper labels/////////////////////
        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
           // userSteps.setText(String.valueOf(totalStepsFromDataPoints));
            String stepCount = String.valueOf(totalStepsFromDataPoints);
                stepsCounter.setProgress(Integer.parseInt(stepCount));
               double steps = Double.parseDouble(stepCount);
               double value =( steps / sharedPreferences.getInt("Goal",5000)) * 100;
//
                stepsPercentage.setText(value+"% OF GOAL "+ (sharedPreferences.getInt("Goal",5000)));


        } else if (dataSet.getDataType().getName().equals("com.google.distance.delta")) {
            distance.setText(String.format("%.2f", distanceTraveledFromDataPoints/1000.00));
            distanceInMeters = distanceTraveledFromDataPoints;
        }

        else if (dataSet.getDataType().getName().equals("com.google.calories.expended")) {
            calories.setText(String.format("%.2f", kcals/1000.00));
            kCals = kcals;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Resumed app");

        // initialize the step listener
        stepListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Log.d(TAG, "Field: " + field.getName());
                    Log.d(TAG, "Value: " + dataPoint.getValue(field));

                    if (field.getName().equals("steps")) {
                       int currentSteps = stepsCounter.getProgress();
                        currentSteps = currentSteps + dataPoint.getValue(field).asInt();
                        //userSteps.setText(Integer.toString(currentSteps));
                        stepsCounter.setProgress(currentSteps);
                        double steps = Double.parseDouble(String.valueOf(currentSteps));
                        double value =( steps / sharedPreferences.getInt("Goal",5000)) * 100;
//
                        stepsPercentage.setText(value+"% OF GOAL "+ (sharedPreferences.getInt("Goal",5000)));


                    }
                }
            }
        };

        // register the step listener
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount((AppCompatActivity) this))) {
            Log.d(TAG, "Not signed in...");
        } else {
            Fitness.getSensorsClient((AppCompatActivity) this, GoogleSignIn.getLastSignedInAccount((AppCompatActivity) this))
                    .add(
                            new SensorRequest.Builder()
                                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                    .setSamplingRate(10, TimeUnit.SECONDS)
                                    .build(), stepListener
                    )
                    .addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Step Listener Registered.");
                                    } else {
                                        Log.e(TAG, "Step Listener not registered", task.getException());
                                    }
                                }
                            }
                    );
        }
    }




    private void retrieveUserDetails(String uid) {

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String fName, lName,gender,fromHour,toHour,lunchHour;
                        Double weight,height;
                        Long age;
                        /////// GET INFO FROM FIRESTORE
                        fName = document.getString("FirstName");
                        lName = document.getString("LastName");
                        gender = document.getString("Gender");
                        fromHour = document.getString("FromHour");
                        toHour = document.getString("ToHour");
                        lunchHour=document.getString("LunchHour");
                        weight=document.getDouble("Weight");
                        height=document.getDouble("Height");
                        age = document.getLong("Age");



                        saveToLocalDB(fName,lName,gender,fromHour,toHour,lunchHour,weight,height,age);



                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }


    /////// SAVE INTO LOCAL DB

    private void saveToLocalDB(String fName, String lName, String gender, String fromHour, String toHour, String lunchHour, Double weight,Double height, Long age) {


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FirstName",fName);
        editor.putString("LastName",lName);
        editor.putString("Gender",gender);
        editor.putString("FromHour",fromHour);
        editor.putString("ToHour",toHour);
        editor.putString("LunchHour",lunchHour);
        editor.putFloat("Weight",Float.valueOf(String.valueOf(weight)));
        editor.putFloat("Height",Float.valueOf(String.valueOf(height)));
        editor.putLong("Age",age);
        editor.apply();

        helloText.setText("Hello there "+ fName +" !");
    }









    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        mClient.disconnect();
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
       else if (id == R.id.nav_about){
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
            finish();
        }
        else if (id == R.id.nav_awards){
            startActivity(new Intent(HomeActivity.this, AchievementsActivity.class));
            finish();
        }
        else if (id == R.id.nav_settings){
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            finish();
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                Log.d(TAG, "accessing...");
                accessGoogleFit();
            }
        }
    }


}
