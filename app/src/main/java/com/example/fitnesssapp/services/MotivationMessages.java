package com.example.fitnesssapp.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.androdocs.httprequest.HttpRequest;
import com.example.fitnesssapp.AppController;
import com.example.fitnesssapp.HomeActivity;
import com.example.fitnesssapp.Locations.APIClient;
import com.example.fitnesssapp.Locations.GoogleMapAPI;
import com.example.fitnesssapp.Locations.NearbyGyms;
import com.example.fitnesssapp.Locations.NearbyMalls;
import com.example.fitnesssapp.Locations.NearbyParks;
import com.example.fitnesssapp.Locations.NearbyRestaurants;
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
    int steps, currenthour, lunchHour, eodHour;
    double temp, humidity;
    float activemins;
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



    public MotivationMessages() {

        appController = new AppController();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());


    }




    @Override
    public void onCreate() {

        Log.d(TAG,"LATITUDE:"+latitude+"LONGITUDE"+longitude);

        sharedPreferences = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        userID = auth.getCurrentUser().getUid();
        username = sharedPreferences.getString("NickName",null);
    }
    private Runnable init = new Runnable() {
        @Override
        public void run() {

            accessHourlySteps();
            accessGoogleFit();
            fetchNearbyLocation("");
//            fetchNearbyLocation("mall");
////            fetchNearbyLocation("gym");
////            fetchNearbyLocation("park");
            checkWeather();
            if(!isActive()){

                startMotivating();
            }
            checkEOD();
            mHandler.postDelayed(this, 300000);
        }
    };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.context = this;
       init.run();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private boolean isActive(){

        if((totalStepsFromDataPoints-initialSteps)<5){
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


        float distanceTraveledFromDataPoints = 0;
        float kcals = 0;
        long mins = 0;
        String startTime = "";
        String endTime = "";


        for (DataPoint dp : dataSet.getDataPoints()) {

            startTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            endTime = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));





            for (Field field : dp.getDataType().getFields()) {
              //  Log.d(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));

                // increment the steps or distance
                if (field.getName().equals("steps")) {

                    totalStepsFromDataPoints = dp.getValue(field).asInt();
//                    if(currenthour <= 10){
//                        initialSteps = totalStepsFromDataPoints;
//                    }



                } else if (field.getName().equals("distance")) {
                    distanceTraveledFromDataPoints += dp.getValue(field).asFloat();
                } else if (field.getName().equals("calories")) {
                    kcals += dp.getValue(field).asFloat();
                }


            }

          //  homeActivity.checkForRewards();
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
//            Log.d(TAG, "Data point:");
//            Log.d(TAG, "\tType: " + dp.getDataType().getName());
//            Log.d(TAG, "\tStart: " + startTime);
//            Log.d(TAG, "\tEnd: " + endTime);
//            Log.d(TAG, "\tTime stamp: " + stime);


            for (Field field : dp.getDataType().getFields()) {
       //         Log.d(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));

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
                       //                 Log.d(TAG,"==============================steps per hour fetched");

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
                //TODO : DELETE THIS LINE (FOR TESTING PURPOSE ONLY)
                category = "category K";
                //fetchNearbyLocation("restaurant");
                Log.d(TAG,"WEEEEEKEND");
            }
            else{
                /**IS LUNCHBREAK SOON?**/
                if(breaktimeDiff == 1){

                    Log.d(TAG,"LUNCH BREAK SOON");
                    checkWeather();
                   if(isWeatherGood){
//                       fetchNearbyLocation("restaurant");
                       if(restaurantlocationNames.size()>0) {
                           category = "category H";
                           Log.d(TAG,"WALK TO RESTAURANT");

                       }
                       else{
                           category = "category I";
                           Log.d(TAG,"CAT: I");
                       }
                   }
                   else{
                       category = "category I";
                       Log.d(TAG,"CAT: I");
                   }

                }
                /**IS EOWD SOON?**/
                else{
                    if(EODtimediff == 1){
                        Log.d(TAG,"END OF DAY COMING SOOON");
                        checkWeather();
                        if(isWeatherGood){
//                            fetchNearbyLocation("park");
                            /////////nearby parks available?
                            if(parklocationNames.size()>0) {
                                category = "category A";

                                Log.d(TAG,"WALK TO PARK");
                            }
                            else{
                               ////walk in mall/gym/street
                                category="category B";
                                Log.d(TAG,"WALK IN MALL");
                            }
                        }
                        else{

                            ///nearby gyms available

//                            fetchNearbyLocation("gym");
                            if(gymlocationNames.size()>0){
                                category = "category C";
                                Log.d(TAG,"WORKOUT IN GYM");
                            }
                            else{
                                ///no nearby gyms
//                                fetchNearbyLocation("shopping_mall");
                                if(malllocationNames.size()>0){
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
                checkWeather();
                if(isWeatherGood){
                    fetchNearbyLocation("park");
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
    }


    @SuppressLint("MissingPermission")
    private void fetchNearbyLocation(String locationtype) {
        this.type = locationtype;
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
            final String currentLocation = lat + "," + lon;
            latitude = lat;
            longitude = lon;
            int radius = 500;

//            GoogleMapAPI googleMapAPI = APIClient.getClient().create(GoogleMapAPI.class);
//            googleMapAPI.getNearBy(currentLocation, radius, type, key).enqueue(new Callback<PlacesResult>() {
//                @Override
//                public void onResponse(Call<PlacesResult> call, Response<PlacesResult> response) {
//                    if (response.isSuccessful()) {
//
//                        String restaurantName, parkName, gymName, mallName;
//                        List<Result> results = response.body().getResults();
//
//
//                        int count = 0;
//
//
//                        Log.d(TAG,"FETCHING LOCATIONS NEARBY...");
//                        Log.d(TAG,currentLocation);
//                        while(results.size()>count){
//                            if(type =="restaurant") {
//                                NearbyRestaurants nearbyRestaurants = new NearbyRestaurants(getApplicationContext(), results);
//                                restaurantName = nearbyRestaurants.getLocationName(count);
//                                restaurantlocationNames.add(restaurantName);
//                                count++;
//                            }
//                            else if(type =="park") {
//
//                                NearbyParks nearbyParks = new NearbyParks(getApplicationContext(), results);
//                                parkName = nearbyParks.getLocationName(count);
//                                parklocationNames.add(parkName);
//                                count++;
//                            }
//                            else if(type =="gym") {
//                                NearbyGyms nearbyGyms = new NearbyGyms(getApplicationContext(),results);
//                                gymName = nearbyGyms.getLocationName(count);
//                                gymlocationNames.add(gymName);
//                                count++;
//                            }
//                            else if(type =="shopping_mall") {
//
//                                NearbyMalls nearbyMalls = new NearbyMalls(getApplicationContext(), results);
//                                mallName = nearbyMalls.getLocationName(count);
//                                malllocationNames.add(mallName);
//                                count++;
//                            }
//                        }
//
//                            Log.d("====RESTAURANTS ====", String.valueOf(restaurantlocationNames));
//                        Log.d("====PARKS====", String.valueOf(parklocationNames));
//                        Log.d("====GYMS====", String.valueOf(gymlocationNames));
//                        Log.d("====MALLS====", String.valueOf(malllocationNames));
//
//
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<PlacesResult> call, Throwable t) {
//                    Toast.makeText(getApplicationContext(), "LOCATION FETCHER: "+t.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            });


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
                if (parklocationNames.size()>0) {
                    retrieveMessage(messagelist);
                }
                if (malllocationNames.size()>0) {
                    retrieveMessage(messagelist);
                }
                if (gymlocationNames.size()>0) {
                    retrieveMessage(messagelist);
                }
                retrieveMessage(messagelist);

            }
        });
     }

     public void retrieveMessage(List<String> messagelist){

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


                 if (messageTitle.contains("<") || message.contains("<")) {

                     Log.d(TAG,"REPLACE TOKEN");
                     replaceTokens();
                 }
                 else{
                     Log.d(TAG,"NO TOKEN");
                     displayNotification();
                 }


             }
         });

     }

    private void replaceTokens() {

//        int i = new Random().nextInt(restaurantlocationNames.size());
//        String randomRestaurant = restaurantlocationNames.get(i);
//        int j = new Random().nextInt(parklocationNames.size());
//        String randomPark = parklocationNames.get(j);
//        int k = new Random().nextInt(gymlocationNames.size());
//        String randomGym = gymlocationNames.get(k);
//        int l = new Random().nextInt(malllocationNames.size());
//        String randomMall = malllocationNames.get(l);
//        //Replace <weather>
//        if(message.contains("<weather>")){
//            message = message.replaceAll("<weather>",temp+" °C");
//        }
//        if(messageTitle.contains("<weather>")){
//            messageTitle = messageTitle.replaceAll("<weather>",temp+" °C");
//        }
//        //Replace <restaurant>
//        if(message.contains("<restaurant>")){
//            message = message.replaceAll("<restaurant>",randomRestaurant);
//        }
//        if(messageTitle.contains("<restaurant>")){
//            messageTitle = messageTitle.replaceAll("<restaurant>", randomRestaurant);
//        }
//        //Replace <park>
//        if(message.contains("<park>")){
//            message = message.replaceAll("<park>",randomPark);
//        }
//        if(messageTitle.contains("<park>")){
//            messageTitle = messageTitle.replaceAll("<park>", randomPark);
//        }
//        //Replace <gym>
//        if(message.contains("<gym>")){
//            message = message.replaceAll("<gym>",randomGym);
//        }
//        if(messageTitle.contains("<gym>")){
//            messageTitle = messageTitle.replaceAll("<gym>", randomGym);
//        }
//        //Replace <mall>
//        if(message.contains("<mall>")){
//            message = message.replaceAll("<park>",randomMall);
//        }
//        if(messageTitle.contains("<mall>")){
//            messageTitle = messageTitle.replaceAll("<mall>", randomMall);
//        }

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
                checkWeather();





            } catch (JSONException e) {

                Log.d("=======WEATHER=========", e.getMessage());
            }

        }
    }

    public void displayNotification() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("fitnessapp", "fitnessapp", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "fitnessapp")
                .setContentTitle(messageTitle)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_person_walk);
        manager.notify(0, builder.build());



    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
