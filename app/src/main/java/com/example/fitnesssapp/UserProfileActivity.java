package com.example.fitnesssapp;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.fitnesssapp.Authentication.ProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private EditText inputFName, inputLName, inputWeight
            ,inputGoal,inputHeight, inputAge, inputFromHour, inputToHour, inputLunchHour;
    private Spinner  inputGender;
    private String  gender, amPm;
    SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_user_profile));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfileActivity.this, HomeActivity.class));
                finish();
            }
        });

        inputFName = (EditText) findViewById(R.id.firstname);
        inputLName = (EditText) findViewById(R.id.lastname);
        inputWeight = (EditText) findViewById(R.id.weight);
        inputHeight = (EditText) findViewById(R.id.height);
        inputAge = (EditText) findViewById(R.id.age);
        inputGender = (Spinner) findViewById(R.id.genderPicker);
        inputGoal = (EditText) findViewById(R.id.goal);
        inputFromHour =  findViewById(R.id.fromHourPicker);
        inputToHour = findViewById(R.id.toHourPicker);
        inputLunchHour = findViewById(R.id.lunchHourPicker);
        inputFName.setText(sharedPreferences.getString("FirstName",null));
        inputLName.setText(sharedPreferences.getString("LastName",null));
        inputAge.setText(String.valueOf(sharedPreferences.getLong("Age",0)));
        inputHeight.setText(String.valueOf( sharedPreferences.getFloat("Height",0)));
        inputWeight.setText(String.valueOf( sharedPreferences.getFloat("Weight",0)));
        inputGender.setSelection(sharedPreferences.getInt("genderSelection",0));
        inputGoal.setText(String.valueOf(sharedPreferences.getInt("Goal",5000)));
        inputFromHour.setText(sharedPreferences.getString("FromHour","00:00"));
        inputToHour.setText(sharedPreferences.getString("ToHour","00:00"));
        inputLunchHour.setText(sharedPreferences.getString("LunchHour","00:00"));

        inputGender.setOnItemSelectedListener(this);
        inputFromHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(UserProfileActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }
                            inputFromHour.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                    }
                }, 0, 0, false);

                timePickerDialog.show();

            }
        });
        inputToHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(UserProfileActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }
                        inputToHour.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                    }
                }, 0, 0, false);

                timePickerDialog.show();

            }
        });
        inputLunchHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(UserProfileActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }
                        inputLunchHour.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                    }
                }, 0, 0, false);

                timePickerDialog.show();

            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo(view);
            }
        });
    }

    private void saveInfo(View view) {

        final String fName, lName, fromHour, toHour,lunchHour;
        final float weight;
        final  float height;
        final  int  selectedGender, goal ;
        final Long age;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        fName = inputFName.getText().toString();
        lName = inputLName.getText().toString();
        weight = Float.parseFloat(inputWeight.getText().toString());
        height = Float.parseFloat(inputHeight.getText().toString());
        age = Long.valueOf(inputAge.getText().toString());
        goal = Integer.parseInt(inputGoal.getText().toString());
        fromHour = inputFromHour.getText().toString();
        toHour = inputToHour.getText().toString();
        lunchHour = inputLunchHour.getText().toString();

        selectedGender = inputGender.getSelectedItemPosition();

        editor.putString("gender", gender);
        editor.putInt("Goal", goal);
        editor.putInt("genderSelection", selectedGender);
        editor.putString("FirstName",fName);
        editor.putString("LastName",lName);
        editor.putFloat("Weight",weight);
        editor.putFloat("Height",height);
        editor.putLong("Age",age);
        editor.putString("FromHour",fromHour);
        editor.putString("ToHour",toHour);
        editor.putString("LunchHour",lunchHour);
        editor.apply();

        ////////Save the info to Firestore
        Map< String, Object > user = new HashMap<>();
        user.put("FirstName",fName);
        user.put("LastName",lName);
        user.put("Weight",weight);
        user.put("Height",height);
        user.put("Age",age);
        user.put("Gender",gender);
        user.put("Goal",goal);
        user.put("FromHour",fromHour);
        user.put("ToHour",toHour);
        user.put("LunchHour",lunchHour);

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(UserProfileActivity.this, "YAY", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(UserProfileActivity.this, "OH NO", Toast.LENGTH_SHORT).show();
                    }
                });
        Snackbar.make(view, "Profile saved!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        gender = adapterView.getItemAtPosition(i).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
