package com.example.fitnesssapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.fitnesssapp.R;
import com.google.firebase.messaging.RemoteMessage;

public class AppWorker extends Worker {

    //This class contains the works that needs to be done in the background


    ////////// INCOMPLETE !!!!!! /////////////////////

    public AppWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
     //   displayNotification("Hello there","We're counting your steps");
        logDailySteps();

        return Result.success();
    }


    private void logDailySteps() {

    }


    private void displayNotification(String task, String desc){

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel("fitnessapp","fitnessapp",NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"fitnessapp")
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.mipmap.ic_launcher);
        manager.notify(1, builder.build());
    }


}
