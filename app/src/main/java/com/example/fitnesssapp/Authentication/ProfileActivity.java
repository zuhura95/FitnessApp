package com.example.fitnesssapp.Authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fitnesssapp.*;

public class ProfileActivity extends AppCompatActivity {

    private EditText inputFName, inputLName, inputWeight, inputHeight, inputAge;
    private Button saveButton;
//    private SessionManager session;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
//        session = new SessionManager(getApplicationContext());
//        sharedPreferences = getSharedPreferences("Account",MODE_PRIVATE);



        inputFName = (EditText) findViewById(R.id.firstname);
        inputLName = (EditText) findViewById(R.id.lastname);
        inputWeight = (EditText) findViewById(R.id.weight);
        inputHeight = (EditText) findViewById(R.id.height);
        inputAge = (EditText) findViewById(R.id.age);
        saveButton = (Button) findViewById(R.id.save_btn);





        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inputFName.getText()== null || inputLName.getText() == null){

                    Toast.makeText(ProfileActivity.this, "Please Enter your Name", Toast.LENGTH_SHORT).show();
                }
                else{
                    getUserProfile();
                }

            }
        });



    }

    private void getUserProfile(){

        final String fName, lName;
        final float weight;
       final  float height;
       final  int age ;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        fName = inputFName.getText().toString();
        lName = inputLName.getText().toString();
        weight = Float.parseFloat(inputWeight.getText().toString());
        height = Float.parseFloat(inputHeight.getText().toString());
        age = Integer.parseInt(inputAge.getText().toString());


        editor.putString("FirstName",fName);
        editor.putString("LastName",lName);
        editor.putFloat("Weight",weight);
        editor.putFloat("Height",height);
        editor.putInt("Age",age);
        editor.apply();
        Toast.makeText(ProfileActivity.this, "Profile Saved", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            startActivity(new Intent(ProfileActivity.this, PermissionsActivity.class));
        }
        else {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
        }







    }
}
