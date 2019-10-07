package com.example.fitnesssapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fitnesssapp.*;
import com.example.fitnesssapp.Authentication.ProfileActivity;
import com.example.fitnesssapp.Authentication.LoginActivity;
import com.example.fitnesssapp.Locations.LocationsActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    private FirebaseAuth auth;
    SharedPreferences sharedPreferences;
    TextView helloText;
    TextView t_v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_home));
        setSupportActionBar(toolbar);
        //Firebase auth instance
        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        helloText = (TextView)headerView.findViewById(R.id.helloTextView);
        String name =sharedPreferences.getString("FirstName",null);
        helloText.setText("Hello there "+ name+" !");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Is the User logged in already?
        if (auth.getCurrentUser() == null) {

            //then reedirect to home page
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        // Get token
        // [START retrieve_current_token]
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Toast.makeText(HomeActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
        // [END retrieve_current_token]

        // [START fcm_runtime_enable_auto_init]
      //  FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        // [END fcm_runtime_enable_auto_init]

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();
        if (id == R.id.nav_profile){
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
            finish();
        }
       else if (id == R.id.nav_locations){
            startActivity(new Intent(HomeActivity.this, LocationsActivity.class));
            finish();
        }
       else if (id == R.id.nav_about){
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
            finish();
        }
        else if (id == R.id.nav_awards){
            startActivity(new Intent(HomeActivity.this, AchievementsActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }
}
