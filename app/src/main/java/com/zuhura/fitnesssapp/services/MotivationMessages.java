package com.zuhura.fitnesssapp.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.androdocs.httprequest.HttpRequest;
import com.zuhura.fitnesssapp.AppController;
import com.zuhura.fitnesssapp.HomeActivity;
import com.zuhura.fitnesssapp.R;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MotivationMessages extends Service {

    private String TAG = "================MOTIVATION MESSAGES================";
    private String weather_API_key = "7a7f09f95d97e3e22d688438853d05f2";
    private int totalStepsFromDataPoints, currentSteps,initialSteps ;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    Context context;
    String category;
    String  message, messageType, messageTitle, type, userID, weekend, lunchbreak, EOD,today,weatherDesc,latitude,longitude,username;
    int steps, currenthour, lunchHour, eodHour, id;
    double temp, humidity;
    float activemins;
    String receiveTime,dismissTime;
    boolean isWeatherGood;
    AppController appController;
    HomeActivity homeActivity;
    private Handler mHandler = new Handler();
    private int notifid=0;
    private int breaktimeDiff;
    private int EODtimediff;
    private LocationManager locationmanager = null;
    List<String> restaurantlocationNames = new ArrayList<>();
    List<String> parklocationNames = new ArrayList<>();
    List<String> gymlocationNames = new ArrayList<>();
    List<String> malllocationNames = new ArrayList<>();
    private int goal,movemins,stepsRemaining;
    private double percentFinished,remainingPercentage;
    private int radius=500;


    public MotivationMessages() {

        appController = new AppController();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());


    }




    @Override
    public void onCreate() {

        fetchLocation();
//        NotificationDismissReceiver notificationDismissReceiver = new NotificationDismissReceiver();
        Log.d(TAG,"LATITUDE:"+latitude+"LONGITUDE"+longitude);
        sharedPreferences = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        userID = auth.getCurrentUser().getUid();
        username = sharedPreferences.getString("NickName",null);
        goal = sharedPreferences.getInt("Goal",5000);
        id = sharedPreferences.getInt("logID",0);
    }

    //Initial run- fetch lat and long and weather continuously
    private Runnable init = new Runnable() {
        @Override
        public void run() {
                fetchLocation();
                checkWeather();
                new nearbyGyms().execute();
                new nearbyMalls().execute();
               new nearbyParks().execute();
                new nearbyRestaurants().execute();

            int delayMs;
            if (latitude == null && longitude == null){
                delayMs = 5000;
            }else{
                delayMs = 300000;
            }
            mHandler.postDelayed(this, delayMs); //5 secs
        }
    };

    //Start motivation after 50 mins
    private Runnable run_motivation = new Runnable() {
        @Override
        public void run() {

            accessHourlySteps();
            accessGoogleFit();

            if(sharedPreferences.getBoolean("notifications",true)) {
                if (!isActive()) {

                    startMotivating();
                }
                checkEOD();
            }
            mHandler.postDelayed(this, 3000000); //2 mins
        }
    };

    //Check steps after 30 mins and log data
    private Runnable run_stepsCheck = new Runnable(){

        @Override
        public void run() {
            fetchStepsafterThirty();
            mHandler.postDelayed(this, 3600000 ); //15 mins
        }
    };

    //Save active mins at the EOD
    private Runnable run_activeTimeCheck = new Runnable() {
        @Override
        public void run() {
            fetchEODActivemins();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        accessHourlySteps();
        accessGoogleFit();
            this.context = this;
            init.run();
            run_motivation.run();

            Toast.makeText(this, "The app is now running in the background.", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private boolean isActive(){

        if((totalStepsFromDataPoints-initialSteps)<5){
            initialSteps = totalStepsFromDataPoints;
            Log.d(TAG,"NOT ACTIVE - sTART MOTIVATING");
            return false;
        }
        else{
            initialSteps = totalStepsFromDataPoints;
            Log.d(TAG," ACTIVE");
            return true;
        }
    }
    /**
     * Access Google Fit recordings
     */
    public void accessGoogleFit() {



        // Subscribe to recordings
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //    Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //   Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });


        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //  Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //  Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });

        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_MOVE_MINUTES)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });


        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        DateFormat dateFormat = DateFormat.getDateTimeInstance();

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS,    DataType.AGGREGATE_STEP_COUNT_DELTA)
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
                        //        Log.d(TAG, "successfully got history");
                        getDataSetsFromBucket(dataReadResponse);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //          Log.e(TAG, "failed to get history", e);
                    }
                });


    }
    public void fetchStepsafterThirty() {



        // Subscribe to recordings
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //    Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //   Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });

        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS,DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        // get history
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        //        Log.d(TAG, "successfully got history");
                        if (dataReadResponse.getBuckets().size() > 0) {

                            for (Bucket bucket : dataReadResponse.getBuckets()) {
                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet dataSet : dataSets) {

                                    for (DataPoint dp : dataSet.getDataPoints()) {

                                        for (Field field : dp.getDataType().getFields()) {

                                            // increment the steps or distance
                                            if (field.getName().equals("steps")) {

                                                currentSteps = dp.getValue(field).asInt();
                                                SharedPreferences.Editor e = sharedPreferences.edit();
                                                e.putInt("currentSteps30",currentSteps);
                                                e.apply();

                                                //TODO Start intent of LogUserDAta
                                                getLogData();
                                            }
                                        }

                                    }

                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //          Log.e(TAG, "failed to get history", e);
                    }
                });


    }
    public void fetchEODActivemins() {



        // Subscribe to recordings
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_MOVE_MINUTES)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "Successfully subscribed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log.d(TAG, "There was a problem subscribing...", e);
                    }
                });

        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
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
                        //        Log.d(TAG, "successfully got history");
                        if (dataReadResponse.getBuckets().size() > 0) {

                            for (Bucket bucket : dataReadResponse.getBuckets()) {
                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet dataSet : dataSets) {

                                    for (DataPoint dp : dataSet.getDataPoints()) {

                                        for (Field field : dp.getDataType().getFields()) {

                                            if(field.getName().equals("duration")){
                                                movemins = dp.getValue(field).asInt();
                                                saveMinsToFirestore();
                                            }
                                        }

                                    }

                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //          Log.e(TAG, "failed to get history", e);
                    }
                });


    }

    private void saveMinsToFirestore() {

        db.collection("users").document(userID)
                .collection(today).document("Active Mins").set(movemins)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getDataSetsFromBucket(DataReadResponse dataReadResponse) {

        if (dataReadResponse.getBuckets().size() > 0) {
            //   Log.d(TAG, "Number of returned buckets of DataSets is: " + dataReadResponse.getBuckets().size());
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
        //  Log.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {

            for (Field field : dp.getDataType().getFields()) {
                //  Log.d(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));

                // increment the steps or distance
                if (field.getName().equals("steps")) {

                    totalStepsFromDataPoints = dp.getValue(field).asInt();
                    double steps = Double.parseDouble(String.valueOf(totalStepsFromDataPoints));
                    percentFinished = (steps / goal) * 100;
                    remainingPercentage = 100-percentFinished;
                    stepsRemaining = goal - totalStepsFromDataPoints;

                    if(currenthour <= 12){
                        initialSteps = totalStepsFromDataPoints;
                    }



                } else if(field.getName().equals("duration")){
                    movemins = dp.getValue(field).asInt();
                }


            }

        }


        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
            //       Toast.makeText(context, "steps: "+totalStepsFromDataPoints, Toast.LENGTH_SHORT).show();


        } else if (dataSet.getDataType().getName().equals("com.google.distance.delta")) {
            //        Toast.makeText(context, "distance: "+distanceTraveledFromDataPoints, Toast.LENGTH_SHORT).show();
        }


    }


    /**
     * Retrieve steps/hour
     */
    private void accessHourlySteps(){

        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        long startTime = cal.getTimeInMillis();


        final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime,endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1,TimeUnit.HOURS)
                .build();



        Fitness.getHistoryClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .readData(dataReadRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        getHourlyStepsFromBucket(dataReadResponse);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //     Log.e(TAG, "failed to get history", e);
            }
        });


    }


    private void getHourlyStepsFromBucket(DataReadResponse readResponse){

        if(readResponse.getBuckets().size()>0){
            //      Log.d(TAG, "/////////Number of returned buckets of DataSets is: " + readResponse.getBuckets().size());
            for (Bucket bucket : readResponse.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();

                for (DataSet dataSet : dataSets) {


                    parseHourlySteps(dataSet);
                }
            }



        }
    }

    /**
     * Parse the datasets
     */
    private void parseHourlySteps(DataSet dataSet) {
        //  Log.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getTimeInstance();

        int totalStepsFromDataPoints = 0;
        String startTime="";
        String stime="";
        String endTime="";

        for (DataPoint dp : dataSet.getDataPoints()) {

            startTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            stime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            endTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));

            for (Field field : dp.getDataType().getFields()) {

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
                        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                        db.collection("users").document(userID)
                                .collection(String.valueOf(today)).document(stime).set(fetchedsteps)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

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

    public void startMotivating(){

        Calendar currentTime = Calendar.getInstance();
        currenthour = currentTime.get(Calendar.HOUR_OF_DAY);


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = simpleDateFormat.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String weekday = sdf.format(date);

        extraData();

        String[] days_array = weekend.split(" ");
        boolean itsWeekend = Arrays.asList(days_array).contains(weekday);

        /**IS it WEEKEND SOON?**/
        if (itsWeekend){
            Log.d(TAG,"WEEEEEKEND");
            if(isWeatherGood){
                radius = 3000;
                category="category K";
                Log.d(TAG,"Outdoor ");
            }else{
                radius = 3000;
                category="category O";
                Log.d(TAG,"Indoor ");
            }

        }
        else{
            /**IS LUNCHBREAK SOON?**/
            if(breaktimeDiff == 1){

                Log.d(TAG,"LUNCH BREAK SOON");
                //   checkWeather();
                if(isWeatherGood){

                    if(restaurantlocationNames.size()>0) {

                        category = "category H";
                        Log.d(TAG,"WALK TO RESTAURANT");

                    }
                    else{
                        radius = 1500;
                        category = "category I";
                        Log.d(TAG,"CAT: I");
                    }
                }
                else{
                    radius = 1500;
                    category = "category N";
                    Log.d(TAG,"Drive/eat at work");
                }

            }
            /**IS EOWD SOON?**/
            else{
                if(EODtimediff == 1){
                    Log.d(TAG,"END OF DAY COMING SOOON");
                    //    checkWeather();
                    if(isWeatherGood){
                        /////////nearby parks available?
                        if(parklocationNames.size()>0) {
                            category = "category A";

                            Log.d(TAG,"WALK TO PARK");
                        }
                        else{
                            radius = 3000;
                            ////walk in mall/gym/street
                            category="category B";
                            Log.d(TAG,"WALK IN MALL");
                        }
                    }
                    else{

                        ///nearby gyms available

                        if(gymlocationNames.size()>0){
                            category = "category C";
                            Log.d(TAG,"WORKOUT IN GYM");
                        }
                        else{
                            ///no nearby gyms

                            if(malllocationNames.size()>0){
                                radius = 3000;
                                category = "category D";
                                Log.d(TAG,"CATEGORY D");
                            }
                            else{
                                ///home exercise
                                category = "category E";
                                Log.d(TAG,"WORKOUT IN HOME");
                            }
                        }
                    }

                }
                else{
                    category = "category J";
                    Log.d(TAG,"CATEGORY J");
                }
            }


        }



            new nearbyGyms().execute();
            new nearbyMalls().execute();
            new nearbyParks().execute();
            new nearbyRestaurants().execute();
            Log.d("=====RADIUS====", String.valueOf(radius));

        retrieveCategoryMessages();

    }

    private void checkEOD(){

        /**IS EOD SOON?  9  pm **/
        if(currenthour == 21){
            Log.d(TAG,"ITS 9 PM");
            if(totalStepsFromDataPoints == sharedPreferences.getInt("Goal",5000)){
                category = "category L";
                Log.d(TAG,"CATEOGRY L");
            }
            else{
                //    checkWeather();
                if(isWeatherGood){
                    if(parklocationNames.size()>0){
                        category="category F";
                        Log.d(TAG,"CATEGORY F");
                    }
                }else{
                    category = "category G";
                    Log.d(TAG,"CATEGORY G");
                }
            }
        }
        if(currenthour == 23){
            run_activeTimeCheck.run();
        }
    }


    @SuppressLint("MissingPermission")
    private void fetchLocation() {

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
            latitude = lat;
            longitude = lon;

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
    private void checkWeather() {

        new weatherTask().execute();
        if((temp > 18) &&(temp < 35)){
            if(humidity < 90){
                isWeatherGood = true;
            }
        }
        else{
            isWeatherGood = false;
        }
        Log.d(TAG,"TEMP"+temp+"HUMIDITY"+humidity);
        Log.d(TAG,"HOW IS WEATHER?"+isWeatherGood);
    }

    private void extraData() {
        weekend= sharedPreferences.getString("Weekend","");
        lunchbreak = sharedPreferences.getString("LunchHour","00");
        EOD = sharedPreferences.getString("ToHour","00");


        SimpleDateFormat hourFormat1 = new SimpleDateFormat("HH:mmaa");
        SimpleDateFormat hourFormat2 = new SimpleDateFormat("HH");
        try {
            Date date = hourFormat1.parse(lunchbreak);
            lunchbreak = hourFormat2.format(date);

            date = hourFormat1.parse(EOD);
            EOD = hourFormat2.format(date);

        } catch (ParseException e) {
        }

        lunchHour = Integer.parseInt(lunchbreak);
        eodHour = Integer.parseInt(EOD);
        breaktimeDiff = lunchHour - currenthour;
        EODtimediff = eodHour - currenthour;




    }

    public void retrieveCategoryMessages(){
        final List<String> messagelist = new ArrayList<>();
        db.collection(category).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        messagelist.add(document.getId());
                    }
                    Log.d(TAG, messagelist.toString());

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

                if (restaurantlocationNames.size()>0) {
                    retrieveMessage(messagelist);
                }
                else if (parklocationNames.size()>0) {
                    retrieveMessage(messagelist);
                }
                else if (malllocationNames.size()>0) {
                    retrieveMessage(messagelist);
                }
                else  if (gymlocationNames.size()>0) {
                    retrieveMessage(messagelist);
                }
                //  retrieveMessage(messagelist);

            }
        });
    }

    public void retrieveMessage(List<String> messagelist){

        if (messagelist.size() > 0) {
            int i = new Random().nextInt(messagelist.size());
            String randomMsg = messagelist.get(i);
            DocumentReference docRef = db.collection(category).document(randomMsg);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            messageTitle = document.getString("title");
                            messageType = document.getString("type");
                            message = document.getString("text");

                        }
                    }


                    if (gymlocationNames != null) {
                        if (parklocationNames != null) {
                            if (restaurantlocationNames != null) {
                                if (malllocationNames != null) {
                                    if (messageTitle.contains("<") || message.contains("<")) {

                                        Log.d(TAG, "REPLACE TOKEN");
                                        replaceTokens();
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "NO TOKEN");
                        displayNotification();
                    }


                }
            });
        }
    }

    private void replaceTokens() {

        int i = new Random().nextInt(restaurantlocationNames.size());
        String randomRestaurant = restaurantlocationNames.get(i);
        int j = new Random().nextInt(parklocationNames.size());
        String randomPark = parklocationNames.get(j);
        int k = new Random().nextInt(gymlocationNames.size());
        String randomGym = gymlocationNames.get(k);
        int l = new Random().nextInt(malllocationNames.size());
        String randomMall = malllocationNames.get(l);
        //Replace <weather>
        if(message.contains("<weather>")){
            message = message.replaceAll("<weather>",temp+" °C");
        }
        if(messageTitle.contains("<weather>")){
            messageTitle = messageTitle.replaceAll("<weather>",temp+" °C");
        }
        //Replace <restaurant>
        if(message.contains("<restaurant>")){
            message = message.replaceAll("<restaurant>",randomRestaurant);
        }
        if(messageTitle.contains("<restaurant>")){
            messageTitle = messageTitle.replaceAll("<restaurant>", randomRestaurant);
        }
        //Replace <park>
        if(message.contains("<park>")){
            message = message.replaceAll("<park>",randomPark);
        }
        if(messageTitle.contains("<park>")){
            messageTitle = messageTitle.replaceAll("<park>", randomPark);
        }
        //Replace <gym>
        if(message.contains("<gym>")){
            message = message.replaceAll("<gym>",randomGym);
        }
        if(messageTitle.contains("<gym>")){
            messageTitle = messageTitle.replaceAll("<gym>", randomGym);
        }
        //Replace <mall>
        if(message.contains("<mall>")){
            message = message.replaceAll("<mall>",randomMall);
        }
        if(messageTitle.contains("<mall>")){
            messageTitle = messageTitle.replaceAll("<mall>", randomMall);
        }

        if(messageTitle.contains("<name>")){
            messageTitle = messageTitle.replaceAll("<name>",username);
        }
        if(message.contains("<name>")){
            messageTitle = messageTitle.replaceAll("<name>",username);
        }
        if(messageTitle.contains("<weather>")){
            messageTitle = messageTitle.replaceAll("<weather>",temp+" °C");
        }
        if(message.contains("<weather>")){
            message = message.replaceAll("<weather>", temp+" °C");
        }
        if(messageTitle.contains("<percentage-finished>")){
            messageTitle = messageTitle.replaceAll("<percentage-finished>",String.format("%.2f", percentFinished)+" %");
        }
        if(message.contains("<percentage-finished>")){
            message = message.replaceAll("<percentage-finished>", String.format("%.2f", percentFinished)+" %");
        }

        if(messageTitle.contains("<percentage-remaining>")){
            messageTitle = messageTitle.replaceAll("<percentage-remaining>",String.format("%.2f", remainingPercentage)+" %");
        }
        if(message.contains("<percentage-remaining>")){
            message = message.replaceAll("<percentage-remaining>", String.valueOf(stepsRemaining));
        }
        if(messageTitle.contains("<steps-remaining>")){
            messageTitle = messageTitle.replaceAll("<steps-remaining>", String.valueOf(stepsRemaining));
        }
        if(message.contains("<steps-remaining>")){
            message = message.replaceAll("<steps-remaining>", String.valueOf(stepsRemaining));
        }
        if(messageTitle.contains("<steps-goal>")){
            messageTitle = messageTitle.replaceAll("<steps-goal>", String.valueOf(goal));
        }
        if(message.contains("<steps-goal>")){
            message = message.replaceAll("<steps-goal>", String.valueOf(goal));
        }

        displayNotification();

    }

    /**
     * Weather forecast
     */
    class weatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }



        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=Doha&units=metric&appid=" + weather_API_key);
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
                temp = Double.parseDouble(main.getString("temp"));
                humidity = Double.parseDouble(main.getString("humidity"));
                String weatherDescription = weather.getString("description");

                String address = jsonObj.getString("name") + ", " + sys.getString("country");



                Log.d("=======WEATHER=========","wind: "+wind+", weather: "+weather+", temp:"+ temp);
                //    checkWeather();





            } catch (JSONException e) {

                Log.d("=======WEATHER=========", e.getMessage());
            }

        }
    }

    class nearbyGyms extends AsyncTask<String, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected String doInBackground(String... strings) {
            String targetURL = "https://places.cit.api.here.com/places/v1/browse?app_id=rN8Lww7j0n8vhpWI46R6&app_code=2QvPBHZjstQyTFCa_UI6Pw&in="+latitude+","+longitude+";r="+radius+"&pretty&cat=sports-facility-venue";
            String response= HttpRequest.excuteGet(targetURL);
            Log.d(TAG,targetURL);
          //  String response= HttpRequest.excuteGet("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius="+radius+"&type=gym&key=AIzaSyA6_HxNGgmNWJlN1cjW5Ugng0FaQFC-Fhs&language=en");
            // String response= HttpRequest.excuteGet("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=25.334018380342,51.47405207536987&radius=1000&type="+"restaurant"+"&key=AIzaSyA6_HxNGgmNWJlN1cjW5Ugng0FaQFC-Fhs");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            int count=0;
            try{
                JSONObject jsonObj = new JSONObject(response);
                JSONObject jsonObj2 = jsonObj.getJSONObject("results");
                JSONArray jsonArray = jsonObj2.getJSONArray("items");
                int n = jsonArray.length();
                while(n>count) {
                    JSONObject items = jsonObj2.getJSONArray("items").getJSONObject(count);
                    String loc = items.getString("title");

                    if(!gymlocationNames.contains(loc)) {
                        gymlocationNames.add(loc);
                    }
                    count++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "=======GYMS=======" + gymlocationNames);
        }
    }
    class nearbyParks extends AsyncTask<String, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected String doInBackground(String... strings) {

            String targetURL = "https://places.cit.api.here.com/places/v1/browse?app_id=rN8Lww7j0n8vhpWI46R6&app_code=2QvPBHZjstQyTFCa_UI6Pw&in="+latitude+","+longitude+";r="+radius+"&pretty&cat=leisure-outdoor";
            String response= HttpRequest.excuteGet(targetURL);
            Log.d(TAG,targetURL);
          //  String response= HttpRequest.excuteGet("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius="+radius+"&type=park&key=AIzaSyA6_HxNGgmNWJlN1cjW5Ugng0FaQFC-Fhs&language=en");
            // String response= HttpRequest.excuteGet("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=25.334018380342,51.47405207536987&radius=1000&type="+"restaurant"+"&key=AIzaSyA6_HxNGgmNWJlN1cjW5Ugng0FaQFC-Fhs");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            int count=0;
            try{
                JSONObject jsonObj = new JSONObject(response);
                JSONObject jsonObj2 = jsonObj.getJSONObject("results");
                JSONArray jsonArray = jsonObj2.getJSONArray("items");
                int n = jsonArray.length();
                while(n>count) {
                    JSONObject items = jsonObj2.getJSONArray("items").getJSONObject(count);
                    String loc = items.getString("title");

                    if(!parklocationNames.contains(loc)){
                        parklocationNames.add(loc);
                    }


                    count++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "=======PARKS=======" + parklocationNames);
        }
    }
    class nearbyRestaurants extends AsyncTask<String, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected String doInBackground(String... strings) {
            String targetURL = "https://places.cit.api.here.com/places/v1/browse?app_id=rN8Lww7j0n8vhpWI46R6&app_code=2QvPBHZjstQyTFCa_UI6Pw&in="+latitude+","+longitude+";r="+radius+"&pretty&cat=eat-drink";
            String response= HttpRequest.excuteGet(targetURL);
            Log.d(TAG,targetURL);
         //   String response= HttpRequest.excuteGet("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius="+radius+"&type=restaurant&key=AIzaSyA6_HxNGgmNWJlN1cjW5Ugng0FaQFC-Fhs&language=en");
            // String response= HttpRequest.excuteGet("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=25.334018380342,51.47405207536987&radius=1000&type=restaurant&key=AIzaSyA6_HxNGgmNWJlN1cjW5Ugng0FaQFC-Fhs");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            int count=0;
            try{
                JSONObject jsonObj = new JSONObject(response);
                JSONObject jsonObj2 = jsonObj.getJSONObject("results");
                JSONArray jsonArray = jsonObj2.getJSONArray("items");
                int n = jsonArray.length();
                while(n>count) {
                    JSONObject items = jsonObj2.getJSONArray("items").getJSONObject(count);
                    String loc = items.getString("title");

                    if(!restaurantlocationNames.contains(loc)){
                        restaurantlocationNames.add(loc);
                    }


                    count++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "=======RESTAURANTS=======" + restaurantlocationNames);
        }
    }
    class nearbyMalls extends AsyncTask<String, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected String doInBackground(String... strings) {
            String targetURL = "https://places.cit.api.here.com/places/v1/browse?app_id=rN8Lww7j0n8vhpWI46R6&app_code=2QvPBHZjstQyTFCa_UI6Pw&in="+latitude+","+longitude+";r="+radius+"&pretty&cat=shopping";
            String response= HttpRequest.excuteGet(targetURL);
            Log.d(TAG,targetURL);
          //  String response= HttpRequest.excuteGet("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius="+radius+"&type=shopping_mall&key=AIzaSyA6_HxNGgmNWJlN1cjW5Ugng0FaQFC-Fhs&language=en");
            // String response= HttpRequest.excuteGet("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=25.334018380342,51.47405207536987&radius=1000&type="+"restaurant"+"&key=AIzaSyA6_HxNGgmNWJlN1cjW5Ugng0FaQFC-Fhs");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            int count=0;
            try{
                JSONObject jsonObj = new JSONObject(response);
                JSONObject jsonObj2 = jsonObj.getJSONObject("results");
                JSONArray jsonArray = jsonObj2.getJSONArray("items");
                int n = jsonArray.length();
                while(n>count) {
                    JSONObject items = jsonObj2.getJSONArray("items").getJSONObject(count);
                    String loc = items.getString("title");

                    if(!malllocationNames.contains(loc)){
                        malllocationNames.add(loc);
                    }


                    count++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "=======MALLS=======" + malllocationNames);
        }
    }

    public void displayNotification() {


        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
        receiveTime = sdf.format(new Date());
        Log.d("=====TIME=====", String.valueOf(receiveTime));

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("fitnessapp", "fitnessapp", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, RatingNotification.class);
        intent.putExtra("messageTitle",messageTitle);
        intent.putExtra("message",message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("message",message);
        e.putString("title",messageTitle);
        e.putString("messageType",messageType);
        e.putInt("currentSteps",totalStepsFromDataPoints);
        e.putString("category",category);
        e.putString("type",messageType);
        e.putInt("movemins",movemins);
        e.apply();

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "fitnessapp")
                .setContentTitle(messageTitle)
                .setContentText(message)
                .setVibrate(new long[] { 1000, 1000, 1000 })
                .setLights(Color.WHITE, 3000, 3000)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
               .setDeleteIntent(createOnDismissedIntent(context, notifid))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_person_walk)
                .setSound(alarmSound);
        manager.notify(notifid, builder.build());


        run_stepsCheck.run();


    }

    private PendingIntent createOnDismissedIntent(Context context, int notifid) {
        Intent intent = new Intent(this, NotifDismissReceiver.class);
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("receivetime",receiveTime);
        e.apply();
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notifid, intent, 0);
        return pendingIntent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void getLogData() {
       String logmessage = sharedPreferences.getString("message","");
       String logreceiveTime = sharedPreferences.getString("receivetime",null);
       int logcurrentSteps = sharedPreferences.getInt("currentSteps",0);
       String logtype = sharedPreferences.getString("type",null);
     String   logcategory = sharedPreferences.getString("category",null);
       int  logcurrentSteps30 = sharedPreferences.getInt("currentSteps30",0);
       String lograting = sharedPreferences.getString("rating","null");
        String logisDismissed = sharedPreferences.getString("isDismissed",null);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
        Date logreceive_time=null;
        Date logdismiss_time=null;
        dismissTime = sdf.format(new Date());
        try {
            logdismiss_time = sdf.parse(dismissTime);
            logreceive_time  = sdf.parse(receiveTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long readingduration = Math.abs(logdismiss_time.getTime() - logreceive_time.getTime());
       String readingDuration =  (readingduration % 3600000) / 60000 + " minutes" +" "+ (readingduration)/1000 % 60 + " seconds";

        logDataToFirestore(logmessage,logcategory,logcurrentSteps,logcurrentSteps30,readingDuration,logisDismissed,lograting,logtype,logreceiveTime);

    }

    private void logDataToFirestore(String logmessage, String logcategory, int logcurrentSteps, int logcurrentSteps30, String readingDuration, String logisDismissed, String lograting, String logtype, String logreceiveTime) {

        String fullData =  today + " | " +logreceiveTime + " | " +logcurrentSteps + " | " + goal  + " | " +logcurrentSteps30 + " | " +lograting + " | "+readingDuration + " | " +logisDismissed + " | " +logmessage + " | " +logcategory+" | "+logtype;
        Map<String,Object> today_log = new HashMap<>();
        today_log.put("Date| Time | Current Step Count | Step Goal  |  Step Count after 30 mins | Message Rating by user | Duration Reading the message | Dismissed(Y/N) |  Message Text | Message Type",
                fullData );


        Map<String,Object> data_log = new HashMap<>();
        data_log.put(String.valueOf(id),today_log);

//        data_log.put("Current Step Count",totalStepsFromDataPoints);
//        data_log.put("Step Goal",goal);
//        data_log.put("Message Category",category);
//        data_log.put("Message Type",messageType);
//        data_log.put("Message Text",message);
//        data_log.put("Step Count after 30 mins",currentSteps);

        db.collection("users").document(userID).collection("User Data Log").document("Data Log").set(data_log, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //  Log.d(TAG,"Logging data SUCCEEDED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log.d(TAG,"Logging data FAILED");
                    }
                });

        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putInt("logID",id++);
        e.apply();
    }


}
