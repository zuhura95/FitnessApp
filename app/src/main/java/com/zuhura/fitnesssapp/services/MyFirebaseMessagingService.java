package com.zuhura.fitnesssapp.services;


import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("TAG", "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }


    private void sendRegistrationToServer(String token) {

    }
}
