package com.example.fitnesssapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;


import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import com.example.fitnesssapp.services.AppWorker;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.view.Menu;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{



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
    AppController appController;

    private float distanceInMeters;
    private float kCals;
    private float movemins;

    Button daybtn,weekbtn, monthbtn;
    BarChart chart;
    String today;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_home));
        setSupportActionBar(toolbar);

        appController = new AppController();
//        //Display health tips pop up once a day
//        Calendar calendar = Calendar.getInstance();
//        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
//        SharedPreferences settings = getSharedPreferences("PREFS",0);
//        int lastDay = settings.getInt("day", 0);
//
//        if (lastDay != currentDay) {
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putInt("day", currentDay);
//            editor.commit();
//
//            //run code that will be displayed once a day
//            Toast.makeText(this, "Hello!!!! Can you see me???", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Different!");
//        } else {
//            Toast.makeText(this, "Same day again!", Toast.LENGTH_SHORT).show();
//            Log.d("MYINT", "Current Day: " + currentDay);
//            Log.d("MYINT", "Last Day: " + lastDay);
//
//            startActivity(new Intent(HomeActivity.this, Popup.class));
//
//
//        }
        today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        appController.setToday(today);

        chart = findViewById(R.id.chart);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

        BarDataSet barDataSet = new BarDataSet(dataValue1(),"STEPS");
        BarData barData = new BarData();
        barData.addDataSet(barDataSet);
        //add to bar chart
        chart.setData(barData);
        chart.invalidate();

//        displayDataOnChart();


        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        String uid = auth.getCurrentUser().getUid();
        appController.setUid(uid);



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

        //get the user's steps goal and set it as maximum value for Arc Progress widget
        stepsCounter.setMax(sharedPreferences.getInt("Goal",5000));




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

            //then reedirect to login page
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
            accessHourlySteps();
        }

        retrieveUserDetails(uid);
        displayNotification();

        daybtn = findViewById(R.id.day_button);
        weekbtn = findViewById(R.id.week_button);
        monthbtn = findViewById(R.id.month_button);


        weekbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this, "Show week graph", Toast.LENGTH_SHORT).show();
            }
        });
        monthbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this, "Show month graph", Toast.LENGTH_SHORT).show();
            }
        });


//        //creating a work request
//        final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(AppWorker.class).build();

        daybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                WorkManager.getInstance().enqueue(request);//performs the work
                Toast.makeText(HomeActivity.this, "Show day graph", Toast.LENGTH_SHORT).show();

            }
        });

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(AppWorker.class,1,TimeUnit.HOURS).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueue(request);
        //display status of work
        WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                String status = workInfo.getState().name();
                Log.d(TAG,status);
            }
        });

    }

    private void displayDataOnChart() {




    }

    private void displayNotification() {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("fitnessapp","fitnessapp",NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"fitnessapp")
                .setContentTitle("Keep Staying Fit")
                .setContentText("Fetching Steps")
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_person_walk);
        manager.notify(1, builder.build());
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

    private void accessHourlySteps(){


        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        long startTime = cal.getTimeInMillis();

        final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime,endTime,TimeUnit.MILLISECONDS)
                .bucketByTime(1,TimeUnit.HOURS)
                .build();

        Fitness.getHistoryClient(this,GoogleSignIn.getLastSignedInAccount(this))
                .readData(dataReadRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        getHourlyStepsFromBucket(dataReadResponse);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "failed to get history", e);
            }
        });


    }

    private void getHourlyStepsFromBucket(DataReadResponse readResponse){

        if(readResponse.getBuckets().size()>0){
            Log.d(TAG, "/////////Number of returned buckets of DataSets is: " + readResponse.getBuckets().size());
            for (Bucket bucket : readResponse.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();

                for (DataSet dataSet : dataSets) {


                    parseHourlySteps(dataSet);
                }
            }



        }
    }

    private void parseHourlySteps(DataSet dataSet) {
        Log.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();

        int totalStepsFromDataPoints = 0;
        String startTime="";
        String stime="";
        String endTime="";

        for (DataPoint dp : dataSet.getDataPoints()) {

            startTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            stime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            endTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            Log.d(TAG, "Data point:");
            Log.d(TAG, "\tType: " + dp.getDataType().getName());
            Log.d(TAG, "\tStart: " + startTime);
            Log.d(TAG, "\tEnd: " + endTime);
            Log.d(TAG, "\tTime stamp: " + stime);


            for (Field field : dp.getDataType().getFields()) {
                Log.d(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));

                // increment the steps or distance
                if (field.getName().equals("steps")) {
                    totalStepsFromDataPoints = dp.getValue(field).asInt();

                }

            }
        }

        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {

            for (DataPoint dataPoint : dataSet.getDataPoints()) {




                for (Field field : dataPoint.getDataType().getFields()) {

                    if (field.getName().equals("steps")) {



                        Map<String, Integer> fetchedsteps = new HashMap<>();

                        int s = dataPoint.getValue(field).asInt();
                        fetchedsteps.put("steps", s);

                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users").document(uid)
                                .collection(String.valueOf(today)).document(stime).set(fetchedsteps)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    //    Toast.makeText(HomeActivity.this, "check it out", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                    }
                }
            }


        }
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
        String startTime="";
        String stime="";
        String endTime="";




        for (DataPoint dp : dataSet.getDataPoints()) {

            startTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            stime = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));
            endTime = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));
            Log.d(TAG, "Data point:");
            Log.d(TAG, "\tType: " + dp.getDataType().getName());
            Log.d(TAG, "\tStart: " + startTime);
            Log.d(TAG, "\tEnd: " + endTime);



            mins = dp.getEndTime(TimeUnit.MINUTES) - dp.getStartTime(TimeUnit.MINUTES) ;
            movemins += mins;
            activeTime.setText(String.format("%.2f", movemins/1000.00));


            for (Field field : dp.getDataType().getFields()) {
                Log.d(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));

                // increment the steps or distance
                if (field.getName().equals("steps")) {
                    totalStepsFromDataPoints = dp.getValue(field).asInt();

                } else if (field.getName().equals("distance")) {
                    distanceTraveledFromDataPoints += dp.getValue(field).asFloat();
                }else if (field.getName().equals("calories")) {
                    kcals += dp.getValue(field).asFloat();
                }



            }
        }

        //update the proper labels
        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {

            String stepCount = String.valueOf(totalStepsFromDataPoints);
                stepsCounter.setProgress(Integer.parseInt(stepCount));
               double steps = Double.parseDouble(stepCount);
               double value =( steps / sharedPreferences.getInt("Goal",5000)) * 100;
                stepsPercentage.setText(String.format("%.2f",value)+"% OF GOAL "+ (sharedPreferences.getInt("Goal",5000)));


        } else if (dataSet.getDataType().getName().equals("com.google.distance.delta")) {
            distance.setText(String.format("%.2f", distanceTraveledFromDataPoints/1000.00));
            distanceInMeters = distanceTraveledFromDataPoints;
        }

        else if (dataSet.getDataType().getName().equals("com.google.calories.expended")) {
            calories.setText(String.format("%.2f", kcals/1000.00));
            kCals = kcals;
        }

    }

  private List<BarEntry> dataValue1(){

      final String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}; // Your List / array with String Values For X-axis Labels

// Set the value formatter
      XAxis xAxis = chart.getXAxis();
      xAxis.setValueFormatter(new AxisValueFormatter(weekdays));

        ArrayList<BarEntry> dataValues = new ArrayList<>();
        dataValues.add(new BarEntry(0,0));
      dataValues.add(new BarEntry(0,0));
      dataValues.add(new BarEntry(0,1));
      dataValues.add(new BarEntry(0,40));
      dataValues.add(new BarEntry(0,50));
      dataValues.add(new BarEntry(0,6));
      dataValues.add(new BarEntry(0,0));
      return dataValues;
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
                                    .setDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
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

                        String fName, lName,gender,fromHour,toHour,lunchHour,weekend;
                        Double weight,height;
                        Long age;
                        /////// GET INFO FROM FIRESTORE
                        fName = document.getString("FirstName");
                        lName = document.getString("LastName");
                        gender = document.getString("Gender");
                        fromHour = document.getString("FromHour");
                        toHour = document.getString("ToHour");
                        lunchHour=document.getString("LunchHour");
                        weekend= document.getString("Weekend");
                        weight=document.getDouble("Weight");
                        height=document.getDouble("Height");
                        age = document.getLong("Age");



                        saveToLocalDB(fName,lName,gender,fromHour,toHour,lunchHour,weekend,weight,height,age);



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

    private void saveToLocalDB(String fName, String lName, String gender, String fromHour, String toHour, String lunchHour, String weekend,Double weight,Double height, Long age) {


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FirstName",fName);
        editor.putString("LastName",lName);
        editor.putString("Gender",gender);
        editor.putString("FromHour",fromHour);
        editor.putString("ToHour",toHour);
        editor.putString("LunchHour",lunchHour);
        editor.putString("Weekend", weekend);
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
                accessHourlySteps();
            }
        }
    }


}
