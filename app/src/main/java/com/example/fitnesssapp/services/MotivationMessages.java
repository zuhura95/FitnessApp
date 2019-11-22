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
import com.example.fitnesssapp.Locations.LocationsActivity;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

public class MotivationMessages {


    private FirebaseAuth auth;
    private FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    Context context;
    String category;
    String  message, type, userID, weekend, lunchbreak, EOD,today,weatherDesc;
    int steps, currenthour, lunchHour, eodHour;
    double temp, humidity;
    float activemins;
    boolean isWeatherGood;
    AppController appController;

    private int notifid=0;
    private int breaktimeDiff;
    private int EODtimediff;
    // private String days_array;

    public MotivationMessages(Context context) {
        this.context = context;
        appController = new AppController();

        temp = appController.getTemp();
        humidity = appController.getHumidity();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        sharedPreferences = context.getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());


    }

//    public MotivationMessages() {
//
//    }


    public void startMotivating(){

        Calendar currentTime = Calendar.getInstance();
        currenthour = currentTime.get(Calendar.HOUR);
        userID = appController.getUid();

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
        appController.setType("gym");
        checkWeather();

        String[] days_array = weekend.split(" ");
        boolean itsWeekend = Arrays.asList(days_array).contains(weekday);

            if (itsWeekend){

                category = "category K";
            }
            else{

                if(breaktimeDiff == 1){

                   if(isWeatherGood){

                   }
                   else{
                       category = "category I";
                   }

                }
                else{
                    if(EODtimediff == 1){
                        category = "category J";
                    }
                    else{
                        //weather is good
                        if(isWeatherGood){

                        }
                        else{

                        }

                    }
                }
                category="category A";

            }




        retrieveMessage();

    }


    private void checkWeather() {

        if((temp > 18) &&(temp < 35)){
            if(humidity < 90){
                isWeatherGood = true;
            }
        }
        else{
            isWeatherGood = false;
        }
   //     Toast.makeText(context, "is weather good? "+isWeatherGood, Toast.LENGTH_SHORT).show();
    }

    private void extraData() {
        weekend= sharedPreferences.getString("Weekend","");
        lunchbreak = sharedPreferences.getString("LunchHour","00:00");
        EOD = sharedPreferences.getString("ToHour","00:00");

        SimpleDateFormat hourFormat1 = new SimpleDateFormat("hh:mmaa");
        SimpleDateFormat hourFormat2 = new SimpleDateFormat("hh");
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
