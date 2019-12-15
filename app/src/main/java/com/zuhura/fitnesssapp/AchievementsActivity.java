package com.zuhura.fitnesssapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;

public class AchievementsActivity extends AppCompatActivity {

    ImageView trophy1, trophy2, trophy3, trophy4, medal1, medal2, medal3, medal4, medal5, medal6, medal7, medal8;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_achievements));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AchievementsActivity.this, HomeActivity.class));
                finish();
            }
        });

        trophy1 = findViewById(R.id.trophy_1);
        trophy2 = findViewById(R.id.trophy_2);
        trophy3 = findViewById(R.id.trophy_3);
       trophy4 = findViewById(R.id.trophy_4);
        medal1 = findViewById(R.id.medal_1);
        medal2 = findViewById(R.id.medal_2);
        medal3 = findViewById(R.id.medal_3);
        medal4 = findViewById(R.id.medal_4);
        medal5 = findViewById(R.id.medal_5);
        medal6 = findViewById(R.id.medal_6);
        medal7 = findViewById(R.id.medal_7);
        medal8 = findViewById(R.id.medal_8);

        trophy1.setImageAlpha(50);
        trophy2.setImageAlpha(50);
        trophy3.setImageAlpha(50);
        trophy4.setImageAlpha(50);
        medal1.setImageAlpha(50);
        medal2.setImageAlpha(50);
        medal3.setImageAlpha(50);
        medal4.setImageAlpha(50);
        medal5.setImageAlpha(50);
        medal6.setImageAlpha(50);
        medal7.setImageAlpha(50);
        medal8.setImageAlpha(50);


        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        checkForUnlockedRewards();
    }

    private void checkForUnlockedRewards() {
        if (sharedPreferences.getBoolean("trophy1", false) == true) {
            trophy1.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("trophy2", false) == true) {
            trophy2.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("trophy3", false) == true) {
            trophy3.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("trophy4", false) == true) {
            trophy4.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("medal1", false) == true) {
            medal1.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("medal2", false) == true) {
            medal2.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("medal3", false) == true) {
            medal3.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("medal4", false) == true) {
            medal4.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("medal5", false) == true) {
            medal5.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("medal6", false) == true) {
            medal6.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("medal7", false) == true) {
            medal7.setImageAlpha(255);

        }
        if (sharedPreferences.getBoolean("medal8", false) == true) {
            medal8.setImageAlpha(255);

        }
    }
}