package com.example.fitnesssapp.Authentication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.fitnesssapp.*;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


public class PermissionsActivity extends AppCompatActivity {

    private Button grantBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);


    //Are permissions granted? If yes then dismiss
        if (ContextCompat.checkSelfPermission(PermissionsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            finish();
            return;
        }
        grantBtn = (Button)findViewById(R.id.btn_grant);

        //else, ask for permission
        grantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Dexter.withActivity(PermissionsActivity.this)
                            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    Toast.makeText(PermissionsActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();

                                    //If permissions are granted , direct to Dashboard.
                                    startActivity(new Intent(PermissionsActivity.this, HomeActivity.class));
                                    finish();
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    if(response.isPermanentlyDenied()){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(PermissionsActivity.this);
                                        builder.setTitle("Permission Denied")
                                                .setMessage("Permission to access device location is permanently denied. you need to go to setting to allow the permission.")
                                                .setNegativeButton("Cancel", null)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent();
                                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                                                    }
                                                })
                                                .show();
                                    } else {
                                        Toast.makeText(PermissionsActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            })
                            .check();
                }
        });
    }
}
