package com.example.fitnesssapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;


import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import com.androdocs.httprequest.HttpRequest;
import com.anychart.APIlib;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.example.fitnesssapp.Locations.LocationsActivity;
import com.example.fitnesssapp.services.AppWorker;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.anychart.AnyChart;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
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
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.view.Menu;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fitnesssapp.Authentication.LoginActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    //Constants
    private String TAG = "Fitness";
    private String weather_API_key = "7a7f09f95d97e3e22d688438853d05f2";
    private final int OAUTH_REQUEST_CODE = 200;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private final int FINE_LOCATION_REQUEST_CODE = 101;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;
    private static OnDataPointListener stepListener;
    private static OnDataPointListener distanceListener;
    AppController appController;

    SharedPreferences sharedPreferences;
    TextView helloText, stepsPercentage, dateTextView, calories, distance, activeTime;
    Button daybtn, weekbtn, monthbtn;
    ArcProgress stepsCounter;
    AnyChartView anyChart,weekChart;
    Dialog awardPopup, healthtip;

    private float distanceInMeters;
    private float kCals;
    private float movemins;
    private int totalStepsFromDataPoints = 0;
    String today, uid;
    List<Integer> stepsData = new ArrayList<>();
    List<String> graph_data = new ArrayList<>();
    List<Integer> weekData = new ArrayList<>();
    List<String> week_graph_data = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_home));
        setSupportActionBar(toolbar);

        appController = new AppController();
        today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        appController.setToday(today);
        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);
        uid = auth.getCurrentUser().getUid();
        analytics.setUserId(uid);
        appController.setUid(uid);

        //Is the User logged in already?
        if (auth.getCurrentUser() == null) {

            //then reedirect to login page
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }


        //Display health tips pop up once a day
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int lastDay = sharedPreferences.getInt("day", 0);

        if (lastDay != currentDay) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("day", currentDay);
            editor.apply();

            //run code that will be displayed once a day
            showHealthTip();

        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        helloText = headerView.findViewById(R.id.helloTextView);
        stepsPercentage = findViewById(R.id.stepsPercent);
        dateTextView = findViewById(R.id.todayDate);
        calories = findViewById(R.id.caloriesTextview);
        distance = findViewById(R.id.distanceTextview);
        activeTime = findViewById(R.id.activetimeTextview);
        stepsCounter = findViewById(R.id.arc_progress);
        anyChart = findViewById(R.id.chart);
        weekChart =findViewById(R.id.weekchart);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        daybtn = findViewById(R.id.day_button);
        weekbtn = findViewById(R.id.week_button);
        monthbtn = findViewById(R.id.month_button);
        awardPopup = new Dialog(this);
        healthtip = new Dialog(this);

        //Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM , yyyy");
        String currentDateandTime = sdf.format(new Date());
        dateTextView.setText(currentDateandTime);

        //get the user's steps goal and set it as maximum value for Arc Progress widget
        stepsCounter.setMax(sharedPreferences.getInt("Goal", 5000));


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
        else {
            Log.d(TAG, "Fine Location permission already granted");

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
                .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                .build();

        // check if app has permissions
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            accessGoogleFit();
        }
        displayNotification();
        retrieveUserDetails(uid);
        hourlyDataOnChart(uid);
        weeklyDataChart(uid);


        weekbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showWeekGraph();
                calculateWeeklySteps(uid);
                Toast.makeText(HomeActivity.this, "Showing week graph", Toast.LENGTH_SHORT).show();
            }
        });
        monthbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this, "Showing month graph", Toast.LENGTH_SHORT).show();
            }
        });


        daybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hourlyDataOnChart(uid);
                Toast.makeText(HomeActivity.this, "Showing day graph", Toast.LENGTH_SHORT).show();


            }
        });


        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(AppWorker.class, 1, TimeUnit.MINUTES).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueue(request);
        //display status of work
        WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                String status = workInfo.getState().name();
                Toast.makeText(HomeActivity.this, status, Toast.LENGTH_SHORT).show();
            }
        });


        new weatherTask().execute();


    }

    /**
     * Display ongoing Notification
     */
    private void displayNotification() {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("fitnessapp", "fitnessapp", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "fitnessapp")
                .setContentTitle("Keep Staying Fit")
                .setContentText("Fetching Steps")
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_person_walk);
        manager.notify(1, builder.build());
    }

    /**
     * Retrieve user's details from Firestore
     * @param uid
     */
    private void retrieveUserDetails(String uid) {

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String fName, lName, gender, fromHour, toHour, lunchHour, weekend;
                        Double weight, height;
                        Long age, genderSelection;
                        /////// GET INFO FROM FIRESTORE
                        fName = document.getString("FirstName");
                        lName = document.getString("LastName");
                        gender = document.getString("Gender");
                        fromHour = document.getString("FromHour");
                        toHour = document.getString("ToHour");
                        lunchHour = document.getString("LunchHour");
                        weekend = document.getString("Weekend");
                        weight = document.getDouble("Weight");
                        height = document.getDouble("Height");
                        age = document.getLong("Age");
                        genderSelection = document.getLong("GenderSelection");


                        saveToLocalDB(fName, lName, gender, fromHour, toHour, lunchHour, weekend, weight, height, age, genderSelection);


                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }


    /**
     *
     *If returning user is using new phone, save profile details into local storage
     */
    private void saveToLocalDB(String fName, String lName, String gender, String fromHour, String toHour, String lunchHour, String weekend, Double weight, Double height, Long age, Long genderSelection) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FirstName", fName);
        editor.putString("LastName", lName);
        editor.putString("Gender", gender);
        editor.putString("FromHour", fromHour);
        editor.putString("ToHour", toHour);
        editor.putString("LunchHour", lunchHour);
        editor.putString("Weekend", weekend);
        editor.putFloat("Weight", Float.valueOf(String.valueOf(weight)));
        editor.putFloat("Height", Float.valueOf(String.valueOf(height)));
        editor.putLong("Age", age);
        editor.putLong("GenderSelection", genderSelection);
        editor.apply();

        helloText.setText("Hello there " + fName + " !");
    }

    /**
     * Display health tip pop up
     */
    private void showHealthTip() {

        TextView healthMessage;
        ImageView healthImage;
//        String[] array = context.getResources().getStringArray(R.array.animals_array);
        String[] array = this.getResources().getStringArray(R.array.health_tips);
        // String[] imagearray = this.getResources().getStringArray(R.array.pic_name);
        int i = new Random().nextInt(array.length);
        String randomStr = array[i];

        healthtip.setContentView(R.layout.dailypopup);
        healthImage = healthtip.findViewById(R.id.message_image);
        healthMessage = healthtip.findViewById(R.id.dailymsg);

        healthMessage.setText(randomStr);
//        healthImage.setImageDrawable(drawable);
        healthtip.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        healthtip.show();

    }


    /**
     * Weather forecast
     */
    class weatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        String LAT = appController.getLatitude();
        String LON = appController.getLongitude();

        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + LAT + "&lon=" + LON + "&units=metric&appid=" + weather_API_key);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {


            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
                String temp = main.getString("temp") + "°C";
                String weatherDescription = weather.getString("description");

                String address = jsonObj.getString("name") + ", " + sys.getString("country");

                Toast.makeText(HomeActivity.this, "Today's weather is " + temp + " and it is " + weatherDescription + " at " + address, Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {

                Log.d(TAG, e.getMessage());
            }

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


                }
            }
        };

        // register the step listener
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this))) {
            Log.d(TAG, "Not signed in...");
        } else {
            Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                Log.d(TAG, "accessing...");
                accessGoogleFit();


            }
        }
    }

    /**
     * Access Google Fit recordings
     */
    private void accessGoogleFit() {


        // Subscribe to recordings
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
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


        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
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
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
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

        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
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


        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        Log.d(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.d(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_MOVE_MINUTES, DataType.AGGREGATE_MOVE_MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        // get history
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
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

    /**
     *
     * Parse Dataset and display step count on progress dialog, calories, active minutes and kilometers taken in a day
     */
    private void parseDataSet(DataSet dataSet) {
        Log.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();


        float distanceTraveledFromDataPoints = 0;
        float kcals = 0;
        long mins = 0;
        String startTime = "";
        String stime = "";
        String endTime = "";


        for (DataPoint dp : dataSet.getDataPoints()) {

            startTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            stime = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));
            endTime = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));
            Log.d(TAG, "Data point:");
            Log.d(TAG, "\tType: " + dp.getDataType().getName());
            Log.d(TAG, "\tStart: " + startTime);
            Log.d(TAG, "\tEnd: " + endTime);


            mins = dp.getEndTime(TimeUnit.MINUTES) - dp.getStartTime(TimeUnit.MINUTES);
            movemins += mins;
            activeTime.setText(String.format("%.2f", movemins / 1000.00));


            for (Field field : dp.getDataType().getFields()) {
                Log.d(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));

                // increment the steps or distance
                if (field.getName().equals("steps")) {
                    totalStepsFromDataPoints = dp.getValue(field).asInt();

                } else if (field.getName().equals("distance")) {
                    distanceTraveledFromDataPoints += dp.getValue(field).asFloat();
                } else if (field.getName().equals("calories")) {
                    kcals += dp.getValue(field).asFloat();
                }


            }
        }

        //update the proper labels
        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {


            stepsCounter.setProgress(totalStepsFromDataPoints);
            double steps = Double.parseDouble(String.valueOf(totalStepsFromDataPoints));
            double value = (steps / sharedPreferences.getInt("Goal", 5000)) * 100;
            stepsPercentage.setText(String.format("%.2f", value) + "% OF GOAL " + (sharedPreferences.getInt("Goal", 5000)));


        } else if (dataSet.getDataType().getName().equals("com.google.distance.delta")) {
            distance.setText(String.format("%.2f", distanceTraveledFromDataPoints / 1000.00));
            distanceInMeters = distanceTraveledFromDataPoints;
        } else if (dataSet.getDataType().getName().equals("com.google.calories.expended")) {
            calories.setText(String.format("%.2f", kcals / 1000.00));
            kCals = kcals;
        }
        checkForRewards();

    }

    /**
     *Retrieve steps per hour from Firestore
     */
    private void hourlyDataOnChart(final String uid) {


        CollectionReference documentReference = db.collection("users").document(uid).collection(today);
        documentReference.orderBy("steps").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {

                            String time = document.getId();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss aa");
                            SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh aa");
                            try {
                                Date date = dateFormat.parse(time);
                                time = dateFormat2.format(date);

                            } catch (ParseException e) {
                            }

                            int steps = Integer.parseInt(String.valueOf(document.get("steps")));


                            stepsData.add(steps);
                            graph_data.add(time);


                        } else {
                            Toast.makeText(HomeActivity.this, "No steps for today", Toast.LENGTH_SHORT).show();
                        }

                    }

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }


                showGraph();
                calculateTotalSteps(uid);
            }


        });


    }

    /**
     *Retrieve total steps for one week from Firestore
     */
    private void weeklyDataChart(final String uid) {


        String weekDay;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = simpleDateFormat.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i > -6; --i){

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, i);
            weekDay = simpleDateFormat.format(calendar.getTime());



            CollectionReference documentReference = db.collection("users").document(uid).collection(weekDay);
            documentReference.orderBy("total").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                String weekday = document.getId();
                                int totalsteps = Integer.parseInt(String.valueOf(document.get("total")));
                                weekData.add(totalsteps);
                                week_graph_data.add(weekday);


                            } else {
                                Toast.makeText(HomeActivity.this, "Doesn't exist", Toast.LENGTH_SHORT).show();
                            }

                        }

                    }


                }


            });

    }

    }

    /**
     *Save total steps of the day to Firestore
     */
    private void calculateTotalSteps(String uid) {

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = format.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("E");
        Log.d(TAG, "=============!!!!!!!!!!!!!!===================");
        String weekday = sdf.format(date);
        Log.d(TAG, weekday);

        int sum = 0;
        for (int i = 0; i < stepsData.size(); i++) {
            sum += stepsData.get(i);

        }
        Log.d(TAG, "wwwwwwwwwwwTOTALwwwwwwwwwwww");
        Log.d(TAG, String.valueOf(sum));

        //Log daily total in Firestore
        Map<String, Integer> totalsteps = new HashMap<>();
        totalsteps.put("total", sum);
        db.collection("users").document(uid)
                .collection(today).document(weekday).set(totalsteps)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "wwwwwwwwwwwTOTAL SAVED!!!!wwwwwwwwwwww");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    /**
     *Save total steps of the week to Firestore
     */
    private void calculateWeeklySteps(String uid){


        int sum = 0;
        for (int i = 0; i < weekData.size();i++){
            sum += weekData.get(i);
        }

        Log.d(TAG, "=============WEEK TOTAL===================");
        Log.d(TAG, String.valueOf(sum));


    }

    /**
     *Display steps per hour on graph
     */
    private void showGraph() {


        APIlib.getInstance().setActiveAnyChartView(anyChart);
        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data2 = new ArrayList<>();

        Log.d(TAG, "While Loop");
        int count = 0;
        while (stepsData.size() > count) {

            data2.add(new ValueDataEntry(graph_data.get(count), stepsData.get(count)));
            Log.d(TAG, graph_data.get(count));
            count++;
        }

        Column column = cartesian.column(data2);

        column.tooltip()
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);

        column.pointWidth(10d);


        cartesian.animation(true);

        //Round corner
        column.rendering().point("function() {\n" +
                "    // if missing (not correct data), then skipping this point drawing\n" +
                "    if (this.missing) {\n" +
                "return;\n" +
                "    }\n" +
                "\n" +
                "    // get shapes group\n" +
                "    var shapes = this.shapes || this.getShapesGroup(this.pointState);\n" +
                "    // calculate the left value of the x-axis\n" +
                "    var leftX = this.x - this.pointWidth / 2;\n" +
                "    // calculate the right value of the x-axis\n" +
                "    var rightX = leftX + this.pointWidth;\n" +
                "    // calculate the half of point width\n" +
                "    var rx = this.pointWidth / 2;\n" +
                "\n" +
                "    shapes['path']\n" +
                "    // resets all 'line' operations\n" +
                "    .clear()\n" +
                "    // draw column with rounded edges\n" +
                "    .moveTo(leftX, this.zero)\n" +
                "    .lineTo(leftX, this.value + rx)\n" +
                "    .circularArc(leftX + rx, this.value + rx, rx, rx, 180, 180)\n" +
                "    .lineTo(rightX, this.zero)\n" +
                "    // close by connecting the last point with the first straight line\n" +
                "    .close();\n" +
                "}");


        cartesian.yScale().minimum(0d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.yAxis(0).title("Steps");


        anyChart.setChart(cartesian);
        weekChart.setVisibility(View.GONE);
        anyChart.setVisibility(View.VISIBLE);

    }

    /**
     *Display total steps for one week on graph
     */
    private void showWeekGraph() {

        APIlib.getInstance().setActiveAnyChartView(weekChart);
        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();

        int count = 0;
        while (weekData.size() > count) {

            data.add(new ValueDataEntry(week_graph_data.get(count), weekData.get(count)));
            Log.d(TAG, week_graph_data.get(count));
            count++;
        }

        Column column = cartesian.column(data);

        column.tooltip()
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);


        column.pointWidth(10d);


        cartesian.animation(true);

        //Round corner
        column.rendering().point("function() {\n" +
                "    // if missing (not correct data), then skipping this point drawing\n" +
                "    if (this.missing) {\n" +
                "return;\n" +
                "    }\n" +
                "\n" +
                "    // get shapes group\n" +
                "    var shapes = this.shapes || this.getShapesGroup(this.pointState);\n" +
                "    // calculate the left value of the x-axis\n" +
                "    var leftX = this.x - this.pointWidth / 2;\n" +
                "    // calculate the right value of the x-axis\n" +
                "    var rightX = leftX + this.pointWidth;\n" +
                "    // calculate the half of point width\n" +
                "    var rx = this.pointWidth / 2;\n" +
                "\n" +
                "    shapes['path']\n" +
                "    // resets all 'line' operations\n" +
                "    .clear()\n" +
                "    // draw column with rounded edges\n" +
                "    .moveTo(leftX, this.zero)\n" +
                "    .lineTo(leftX, this.value + rx)\n" +
                "    .circularArc(leftX + rx, this.value + rx, rx, rx, 180, 180)\n" +
                "    .lineTo(rightX, this.zero)\n" +
                "    // close by connecting the last point with the first straight line\n" +
                "    .close();\n" +
                "}");

        cartesian.yScale().minimum(0d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.yAxis(0).title("Steps");

        weekChart.setChart(cartesian);
        anyChart.setVisibility(View.GONE);
        weekChart.setVisibility(View.VISIBLE);

    }

    /**
     * Check if the user has achieved any reward
     */
    public void checkForRewards() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        TextView txtclose, awardMessage;
        ImageView awardImage;

        boolean gotReward = sharedPreferences.getBoolean("trophy1", false);
        awardPopup.setContentView(R.layout.custompopup);
        txtclose = awardPopup.findViewById(R.id.txtclose);
        awardImage = awardPopup.findViewById(R.id.award_image);
        awardMessage = awardPopup.findViewById(R.id.awardmsg);
        Drawable myTrophy = getResources().getDrawable(R.drawable.rewardcup2);
        Drawable myMedal = getResources().getDrawable(R.drawable.rewardmedal);

        float distanceInKm = distanceInMeters / 1000;

        Log.d(TAG, "-------REWARDS-------");
        Log.d(TAG, String.valueOf(gotReward));


        if ((totalStepsFromDataPoints == sharedPreferences.getInt("Goal", 5000))) {

            editor.putBoolean("trophy1", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.complete1));
            awardMessage.setText(R.string.award_1_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();

        }


        //daily steps = steps goal) for 7 days
//        if(stepsCounter.getProgress() == sharedPreferences.getInt("Goal",5000)){
//
//            awardMessage.setText("Achieved daily steps goal for 1 week!");
//            awardPopup.show();
//        }


        //daily steps >= 10000
        if ((totalStepsFromDataPoints >= 10000)) {
            editor.putBoolean("trophy2", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.monkey));
            awardMessage.setText(R.string.award_2_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }

        //daily steps >= 20000
        if ((totalStepsFromDataPoints >= 20000)) {
            editor.putBoolean("trophy3", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.bee));
            awardMessage.setText(R.string.award_3_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }


        //distance higher than 1.5 km
        if ((distanceInKm >= 1.5)) {
            editor.putBoolean("medal1", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.torch));
            awardMessage.setText(R.string.award_5_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }

        //distance higher than 7 km
        if ((distanceInKm >= 7)) {
            editor.putBoolean("medal2", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.cornish));
            awardMessage.setText(R.string.award_6_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }

        //distance higher than 10 km
        if ((distanceInKm >= 10)) {
            editor.putBoolean("medal3", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.janoub));
            awardMessage.setText(R.string.award_7_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }
        //distance higher than 33 km
        if ((distanceInKm >= 33)) {
            editor.putBoolean("medal4", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.alkhor));
            awardMessage.setText(R.string.award_8_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }
        //distance higher than 97 km
        if ((distanceInKm >= 97)) {
            editor.putBoolean("medal5", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.halul));
            awardMessage.setText(R.string.award_9_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }
        //distance higher than 160 km
        if ((distanceInKm >= 160)) {
            editor.putBoolean("medal6", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.length));
            awardMessage.setText(R.string.award_10_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }
        //distance higher than 190 km
        if ((distanceInKm >= 190)) {
            editor.putBoolean("medal7", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.shamal));
            awardMessage.setText(R.string.award_11_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }
        //distance higher than 571 km
        if ((distanceInKm >= 571)) {
            editor.putBoolean("medal8", true);
            awardImage.setImageDrawable(getResources().getDrawable(R.drawable.kuwait));
            awardMessage.setText(R.string.award_12_message);
            awardPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            awardPopup.show();
        }


        editor.apply();
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                awardPopup.dismiss();
            }
        });


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        if (id == R.id.nav_profile) {
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
            finish();
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
            finish();
        } else if (id == R.id.nav_locations) {
            startActivity(new Intent(HomeActivity.this, LocationsActivity.class));
            finish();
        } else if (id == R.id.nav_awards) {
            startActivity(new Intent(HomeActivity.this, AchievementsActivity.class));
            finish();
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            finish();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }



}
