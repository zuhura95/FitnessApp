package com.example.fitnesssapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotifDismissReceiver extends BroadcastReceiver {
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
    String dismissTime,receiveTime;
    SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        dismissTime = sdf.format(new Date());
 sharedPreferences = context.getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
 receiveTime = sharedPreferences.getString("receivetime",null);
 //        receiveTime = intent.getExtras().getString("receive_time");
        Log.d("=====RECEIVE TIME=====", String.valueOf(receiveTime));
        Log.d("=====DISMISS TIME=====", String.valueOf(dismissTime));
    }
}
