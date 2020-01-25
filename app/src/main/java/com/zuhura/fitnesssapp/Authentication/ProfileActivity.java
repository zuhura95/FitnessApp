package com.zuhura.fitnesssapp.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zuhura.fitnesssapp.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText inputNickname, inputWeight, inputHeight, inputAge;
    private Button saveButton;
    SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        inputNickname = (EditText) findViewById(R.id.nickname);
        inputWeight = (EditText) findViewById(R.id.weight);
        inputHeight = (EditText) findViewById(R.id.height);
        inputAge = (EditText) findViewById(R.id.age);
        saveButton = (Button) findViewById(R.id.save_btn);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(!TextUtils.isEmpty(inputNickname.getText().toString())){
                    if(!TextUtils.isEmpty(inputWeight.getText().toString())){
                        if(!TextUtils.isEmpty(inputHeight.getText().toString())){
                            if(!TextUtils.isEmpty(inputAge.getText().toString())) {

                                final String nickName = inputNickname.getText().toString();
                                final float weight = Float.parseFloat(inputWeight.getText().toString());
                                final float height = Float.parseFloat(inputHeight.getText().toString());
                                final int age = Integer.parseInt(inputAge.getText().toString());
                                getUserProfile(nickName, weight, height, age);
                            } else{
                                Toast.makeText(ProfileActivity.this, "Please Enter your Age", Toast.LENGTH_SHORT).show();

                            }
                        }
                        else{
                            Toast.makeText(ProfileActivity.this, "Please Enter your Height", Toast.LENGTH_SHORT).show();

                        }
                    } else{
                        Toast.makeText(ProfileActivity.this, "Please Enter your Weight", Toast.LENGTH_SHORT).show();

                    }
                }

                else{
                    Toast.makeText(ProfileActivity.this, "Please Enter your NickName", Toast.LENGTH_SHORT).show();

                }


            }
        });

    }


    /**
     * Save user's info to local storage
     * @param nickName
     * @param weight
     * @param height
     * @param age
     */
    private void getUserProfile(String nickName, float weight, float height, int age){


        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("NickName",nickName);
        editor.putFloat("Weight",weight);
        editor.putFloat("Height",height);
        editor.putInt("Age",age);
        editor.commit();
        Toast.makeText(ProfileActivity.this, "Profile Saved", Toast.LENGTH_SHORT).show();

        //Save the info to Firestore
        saveToFirebaseDB(nickName,weight,height,age);

        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            startActivity(new Intent(ProfileActivity.this, PermissionsActivity.class));
        }
        else {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
        }

    }

    /**
     * Save user's info to fireestore
     */
    private void saveToFirebaseDB(String nickName, float weight, float height, int age){
        Map< String, Object > user = new HashMap<>();
        user.put("NickName",nickName);
        user.put("Weight",weight);
        user.put("Height",height);
        user.put("Age",age);
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


    }


}
