package com.zuhura.fitnesssapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotifDismissReceiver extends BroadcastReceiver {
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
    String dismissTime,receiveTime, readingDuration;
    Date receive_time,dismiss_time;
    SharedPreferences sharedPreferences;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;

    private int id;
    private String today;
    private int currentSteps;
    private int goal;
    private String type;
    float rating;
    private String category;
    private int currentSteps30;
    private int movemins;
    private String userID;
    private String message;

    @Override
    public void onReceive(Context context, Intent intent) {
        dismissTime = sdf.format(new Date());
         sharedPreferences = context.getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        receiveTime = sharedPreferences.getString("receivetime",null);
        id = sharedPreferences.getInt("logID",0);
        currentSteps = sharedPreferences.getInt("currentSteps",0);
        goal = sharedPreferences.getInt("Goal",5000);
        type = sharedPreferences.getString("type",null);
        category = sharedPreferences.getString("category",null);
        currentSteps30 = sharedPreferences.getInt("currentSteps30",0);
        movemins = sharedPreferences.getInt("movemins",0);
        message = sharedPreferences.getString("message","");

        try {
            dismiss_time = sdf.parse(dismissTime);
          receive_time  = sdf.parse(receiveTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long readingduration = Math.abs(dismiss_time.getTime() - receive_time.getTime());
        readingDuration =  (readingduration % 3600000) / 60000 + " minutes" +" "+ (readingduration)/1000 % 60 + " seconds";
        Log.d("=====RECEIVE TIME=====", String.valueOf(receiveTime));
        Log.d("=====DISMISS TIME=====", String.valueOf(dismissTime));
        Log.d("=====TOTAL TIME=====",readingDuration);

        SharedPreferences.Editor e = sharedPreferences.edit();
       e.putString("isDismissed","Y");
        e.apply();
        recordDismiss();
        
    }
//    private void logDataToFirestore(){
//
//        Map<String,Object> today_log = new HashMap<>();
//        today_log.put("Date| Time | Current Step Count | Step Goal  |  Step Count after 30 mins | Message Rating by user | Duration Reading the message | Dismissed(Y/N) |  Message Text | Message Type  | Active Time",
//                today + " | " +receiveTime + " | " +currentSteps + " | " + goal  + " | " +currentSteps30 + " | " +rating + " | "+readingDuration + " | " +"Y" + " | " +message + " | " +category+" | "+type+" | "+movemins);
//
//        Map<String,Object> data_log = new HashMap<>();
//        data_log.put(String.valueOf(id),today_log);
//
////        data_log.put("Current Step Count",totalStepsFromDataPoints);
////        data_log.put("Step Goal",goal);
////        data_log.put("Message Category",category);
////        data_log.put("Message Type",messageType);
////        data_log.put("Message Text",message);
////        data_log.put("Step Count after 30 mins",currentSteps);
//
//        db.collection("users").document(userID).collection("User Data Log").document("Data Log").set(data_log, SetOptions.merge())
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        //  Log.d(TAG,"Logging data SUCCEEDED");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Log.d(TAG,"Logging data FAILED");
//                    }
//                });
//
//        SharedPreferences.Editor e = sharedPreferences.edit();
//        e.putInt("logID",id++);
//        e.putString("isDismissed","Y");
//        e.apply();
//        recordDismiss();
//    }

    private void recordDismiss(){

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "App_notification_dismiss");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
