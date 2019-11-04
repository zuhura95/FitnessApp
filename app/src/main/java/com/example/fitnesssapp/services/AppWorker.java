package com.example.fitnesssapp.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.fitnesssapp.AppController;
import com.example.fitnesssapp.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppWorker extends Worker {

    Context context;
    FirebaseAuth auth;
    FirebaseFirestore db;
    private String TAG = "================Fitness================";
    AppController appController;
    String uid;


    public AppWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = auth.getCurrentUser().getUid();

    }

    @NonNull
    @Override
    public Result doWork() {
        accessHourlySteps();
        return Result.success();
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
                .setTimeRange(startTime,endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1,TimeUnit.HOURS)
                .build();


        Fitness.getHistoryClient(getApplicationContext(),GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
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
                        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                        db.collection("users").document(uid)
                                .collection(String.valueOf(today)).document(stime).set(fetchedsteps)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    //    Toast.makeText(getApplicationContext(), "check", Toast.LENGTH_SHORT).show();

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


}
