package com.example.fitnesssapp.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private SessionManager session;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(getApplicationContext());
        sharedPreferences = getSharedPreferences("Account",MODE_PRIVATE);


        inputFName = (EditText) findViewById(R.id.firstname);
        inputLName = (EditText) findViewById(R.id.lastname);
        inputWeight = (EditText) findViewById(R.id.weight);
        inputHeight = (EditText) findViewById(R.id.height);
        inputAge = (EditText) findViewById(R.id.age);
        saveButton = (Button) findViewById(R.id.save_btn);

        getUserProfile();


    }

    private void getUserProfile(){

        final String fName, lName;
        final float weight;
        final float height;
        final int age;

        fName = inputFName.getText().toString();
        lName = inputLName.getText().toString();
        weight = Float.parseFloat(inputWeight.getText().toString());
        height = Float.parseFloat(inputHeight.getText().toString());
        age = Integer.parseInt(inputAge.getText().toString());

        if (fName != null){
            if (lName != null){
                if (weight != 0){
                    if (height != 0){
                        if (age != 0){

                            saveButton.isEnabled();

                            saveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    session.setFName(fName);
                                    session.setLName(lName);
                                    session.setWeight(weight);
                                    session.setHeight(height);
                                    session.setAge(age);
                                    Toast.makeText(ProfileActivity.this, "Profile Saved", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));

                                }
                            });




                        }
                        else{
                            Toast.makeText(this, "Please Enter your Age", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(this, "Please Enter your Height", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(this, "Please Enter your Weight", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Please Enter your Last Name", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Please Enter your First Name", Toast.LENGTH_SHORT).show();
        }

    }
}
