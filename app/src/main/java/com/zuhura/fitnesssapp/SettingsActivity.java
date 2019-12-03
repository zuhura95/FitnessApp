package com.zuhura.fitnesssapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    Switch simpleSwitch;
    SharedPreferences sharedPreferences;
    boolean switchState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_settings));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        simpleSwitch = (Switch) findViewById(R.id.Notifswitch);
        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                finish();
            }
        });


        simpleSwitch.setChecked(sharedPreferences.getBoolean("notifications",true));
        // check current state of a Switch (true or false).

        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("notifications", simpleSwitch.isChecked());
                editor.apply();
                if (simpleSwitch.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Notifications for this app is ENABLED", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Notifications for this app is DISABLED", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }
}
