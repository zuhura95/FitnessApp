package com.zuhura.fitnesssapp.services;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LogUserData {

    SharedPreferences sharedPreferences;
    Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;

    private int id;
    private String today;
    private String receiveTime;
    private int currentSteps;
    private int goal;
    private String type;
    String rating;
    private String category;
    private int currentSteps30;
    private int movemins;
    private String userID;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
    String dismissTime, readingDuration;
    Date receive_time,dismiss_time;
    private String message;
    private String isDismissed, fullData;

    public LogUserData(Context context) {
        this.context = context;
    }

    private void init(){
        today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        sharedPreferences = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        getLogData();
    }

    private void getLogData() {
        message = sharedPreferences.getString("message","");
        id = sharedPreferences.getInt("logID",0);
        receiveTime = sharedPreferences.getString("receivetime",null);
        currentSteps = sharedPreferences.getInt("currentSteps",0);
        goal = sharedPreferences.getInt("Goal",5000);
        type = sharedPreferences.getString("type",null);
        category = sharedPreferences.getString("category",null);
        currentSteps30 = sharedPreferences.getInt("currentSteps30",0);
        rating = sharedPreferences.getString("rating","null");
        isDismissed = sharedPreferences.getString("isDismissed",null);
        dismissTime = sdf.format(new Date());
        try {
            dismiss_time = sdf.parse(dismissTime);
            receive_time  = sdf.parse(receiveTime);
        } catch (ParseException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        long readingduration = Math.abs(dismiss_time.getTime() - receive_time.getTime());
        readingDuration =  (readingduration % 3600000) / 60000 + " minutes" +" "+ (readingduration)/1000 % 60 + " seconds";

        logDataToFirestore();

    }
    private void logDataToFirestore(){

        fullData =  today + " | " +receiveTime + " | " +currentSteps + " | " + goal  + " | " +currentSteps30 + " | " +rating + " | "+readingDuration + " | " +isDismissed + " | " +message + " | " +category+" | "+type;
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
