package com.example.fitnesssapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.fitnesssapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MotivationMessages {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    Context context;
    String category = "A";
    String  message, type, userID, weekend, lunchbreak, EOD,today;
    int steps;
    float activemins;

    public MotivationMessages(Context context) {
        this.context = context;
    }


    public void startMotivating(int totalStepsFromDataPoints, float movemins, String uid, String today){

        this.steps = totalStepsFromDataPoints;
        this.activemins = movemins;
        this.userID = uid;
        this.today = today;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = simpleDateFormat.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("E");
        String weekday = sdf.format(date);

        extraData();

        if(activemins <  40){
            if (weekday == weekend){
                    //change weekend to array and check if it exist
                        //if lunch break soon?

                 }
            else{
                category = "K";
            }

        }

    }

    private void extraData() {
        DocumentReference documentReference = db.collection("users").document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                   DocumentSnapshot docsnap = task.getResult();
                   if(docsnap.exists()){

                       lunchbreak = docsnap.getString("LunchHour");
                       EOD = docsnap.getString("ToHour");
                       weekend = docsnap.getString("Weekend");

                   }
                }
            }
        });
    }

    public void retrieveMessage(){
         DocumentReference docRef = db.collection("messages").document(category);
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
        manager.notify(1, builder.build());


    }
}
