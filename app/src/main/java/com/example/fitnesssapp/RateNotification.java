package com.example.fitnesssapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class RateNotification extends AppCompatActivity {

    TextView messageTitle,messageText;
    private String message="";
    private String title="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_notification);

        messageTitle = findViewById(R.id.NotifTitle);
        messageText = findViewById(R.id.Notifmsg);
         message = getIntent().getStringExtra("message");
         title = getIntent().getStringExtra("messageTitle");


        messageTitle.setText(title);
        messageText.setText(message);

    }
}
