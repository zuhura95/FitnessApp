package com.example.fitnesssapp;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private EditText inputNickName, inputWeight
            ,inputGoal,inputHeight, inputAge, inputFromHour, inputToHour, inputLunchHour, inputWeekends;
    private Spinner  inputGender;
    private String  gender, amPm;
    SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    AlertDialog alertDialog;

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

        inputNickName =  findViewById(R.id.nickname);
        inputWeight = findViewById(R.id.weight);
        inputHeight =  findViewById(R.id.height);
        inputAge =  findViewById(R.id.age);
        inputGender =  findViewById(R.id.genderPicker);
        inputGoal =  findViewById(R.id.goal);
        inputFromHour =  findViewById(R.id.fromHourPicker);
        inputToHour = findViewById(R.id.toHourPicker);
        inputLunchHour = findViewById(R.id.lunchHourPicker);
        inputWeekends = findViewById(R.id.weekendsPicker);

        //Set the saved user details as values in the textfields for Profile page
        inputNickName.setText(sharedPreferences.getString("NickName",null));
        inputAge.setText(String.valueOf(sharedPreferences.getLong("Age",0)));
        inputHeight.setText(String.valueOf( sharedPreferences.getFloat("Height",0)));
        inputWeight.setText(String.valueOf( sharedPreferences.getFloat("Weight",0)));
        inputGender.setSelection(sharedPreferences.getInt("genderSelection",0));
        inputGoal.setText(String.valueOf(sharedPreferences.getInt("Goal",5000)));
        inputFromHour.setText(sharedPreferences.getString("FromHour","00:00"));
        inputToHour.setText(sharedPreferences.getString("ToHour","00:00"));
        inputLunchHour.setText(sharedPreferences.getString("LunchHour","00:00"));
        inputWeekends.setText(sharedPreferences.getString("Weekend",""));


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
        inputWeekends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                final CharSequence[] days= {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
                final ArrayList selectedDays = new ArrayList();
                builder.setTitle("Select your Days Off").setMultiChoiceItems(days, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked){
                            selectedDays.add(days[which]);
                        }
                        else if(selectedDays.contains(which)){
                            selectedDays.remove(Integer.valueOf(which));
                        }
                    }
                });

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(Object days:selectedDays){
                             stringBuilder.append(days.toString()+" ");
                        }
                        inputWeekends.setText(stringBuilder.toString());
                    }
                });

                alertDialog = builder.create();
                alertDialog.show();

            }
        });

        //save button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo(view);
            }
        });
    }

    /**
     * Save any changes to user profile to local storage and firestore
     */
    private void saveInfo(View view) {

        final String nickName, fromHour, toHour,lunchHour,weekend;
        final float weight;
        final  float height;
        final  int  selectedGender, goal ;
        final Long age;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        nickName = inputNickName.getText().toString();
        weight = Float.parseFloat(inputWeight.getText().toString());
        height = Float.parseFloat(inputHeight.getText().toString());
        age = Long.valueOf(inputAge.getText().toString());
        goal = Integer.parseInt(inputGoal.getText().toString());
        fromHour = inputFromHour.getText().toString();
        toHour = inputToHour.getText().toString();
        lunchHour = inputLunchHour.getText().toString();
        weekend = inputWeekends.getText().toString();
        selectedGender = inputGender.getSelectedItemPosition();


        //Save the info to local storage
        editor.putString("gender", gender);
        editor.putInt("Goal", goal);
        editor.putInt("genderSelection", selectedGender);
        editor.putString("NickName",nickName);
        editor.putFloat("Weight",weight);
        editor.putFloat("Height",height);
        editor.putLong("Age",age);
        editor.putString("FromHour",fromHour);
        editor.putString("ToHour",toHour);
        editor.putString("LunchHour",lunchHour);
        editor.putString("Weekend",weekend);
        editor.commit();

        //Save the info to Firestore
        Map< String, Object > user = new HashMap<>();
        user.put("NickName",nickName);
        user.put("Weight",weight);
        user.put("Height",height);
        user.put("Age",age);
        user.put("Gender",gender);
        user.put("GenderSelection",selectedGender);
        user.put("Goal",goal);
        user.put("FromHour",fromHour);
        user.put("ToHour",toHour);
        user.put("LunchHour",lunchHour);
        user.put("Weekend",weekend);

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
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
