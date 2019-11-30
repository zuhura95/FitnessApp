package com.example.fitnesssapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class RateNotification extends AppCompatActivity {

    TextView messageTitle,messageText;
   RatingBar ratingBar;
    Button submitRating;
    private String message="";
    private String title="";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_notification);

        sharedPreferences =getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        messageTitle = findViewById(R.id.NotifTitle);
        messageText = findViewById(R.id.Notifmsg);
        ratingBar = findViewById(R.id.ratingBar);
        submitRating = findViewById(R.id.submitBtn);
        message = sharedPreferences.getString("message","");
        title = sharedPreferences.getString("title","");
        messageText.setText(message);
        messageTitle.setText(title);

        submitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.putFloat("rating",rating);
                e.apply();
                startActivity(new Intent(RateNotification.this, HomeActivity.class));
                finish();
            }
        });

    }
}
