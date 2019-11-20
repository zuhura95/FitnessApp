package com.example.fitnesssapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.fitnesssapp.AppController;
import com.example.fitnesssapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;

public class MotivationMessages {


    private FirebaseAuth auth;
    private FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    Context context;
    String category = "A";
    String  message, type, userID, weekend, lunchbreak, EOD,today;
    int steps;
    float activemins;
    AppController appController;
    private int notifid=0;
   // private String days_array;

    public MotivationMessages(Context context) {
        this.context = context;
        appController = new AppController();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        sharedPreferences = context.getSharedPreferences("UserInfo",Context.MODE_PRIVATE);

    }




    public void startMotivating(int totalStepsFromDataPoints, float movemins, String today){

        this.steps = totalStepsFromDataPoints;
        userID = appController.getUid();
        this.today = today;
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

            if (itsWeekend){
                category = "category K";
            }
            else{


            }




        retrieveMessage();

    }

    private void extraData() {
        weekend= sharedPreferences.getString("Weekend","");

    }

    public void retrieveMessage(){
         DocumentReference docRef = db.collection(category).document("msg1");
         docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
             @Override
             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                 if (task.isSuccessful()){
                     DocumentSnapshot documentSnapshot = task.getResult();
                     if (documentSnapshot.exists()){
                         message = documentSnapshot.getString("text");
                         type = documentSnapshot.getString("type");
                     }
                 }

                 displayNotification();
             }
         });
     }

    public void displayNotification() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("fitnessapp", "fitnessapp", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "fitnessapp")
                .setContentTitle(type)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_person_walk);
        manager.notify(0, builder.build());



    }
}
