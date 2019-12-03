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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zuhura.fitnesssapp.HomeActivity;
import com.zuhura.fitnesssapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button signUpBtn, forgotPassBtn, signInBtn;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase authentication instance
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        //Is the User logged in already?
        if (auth.getCurrentUser() != null) {

            //then re-direct to home page
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }


        signInBtn =  findViewById(R.id.btn_login);
        signUpBtn = findViewById(R.id.btn_signup);
        forgotPassBtn =  findViewById(R.id.btn_reset_password);
        inputEmail = findViewById(R.id.email);
        inputPassword =  findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);




        //On clicking 'Forgot password' button
        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPassActivity.class));
            }
        });

        //On clicking 'SIGN UP' button
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
        //On clicking 'SIGN IN' button
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Snackbar.make(v, "Please enter your Email Address", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                 return;
                }

                if (TextUtils.isEmpty(password)) {
                    Snackbar.make(v, "Please enter your password", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Authenticate user

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    // If successful, check if  Permissions for location is granted
                                    //If not granted
                                    if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                                        startActivity(new Intent(LoginActivity.this, PermissionsActivity.class));
                                    }
                                    else {
                                        //If permissions were already granted

                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                } else {
                                    Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();


                                }
                            }
                        });
            }
        });


    }



}
