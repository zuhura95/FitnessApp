package com.example.fitnesssapp.Authentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.util.Log;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "UserInfo";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_USER_FIRSTNAME = "FirstName";
    private static final String KEY_USER_LASTNAME = "LastName";
    private static final String KEY_USER_WEIGHT = "Weight";
    private static final String KEY_USER_HEIGHT = "Height";
    private static final String KEY_USER_AGE ="Age";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void setFName(String Fname) {

        editor.putString(KEY_USER_FIRSTNAME, Fname);
        // commit changes
        editor.commit();


    }
    public void setLName(String Lname) {

        editor.putString(KEY_USER_LASTNAME, Lname);
        // commit changes
        editor.commit();


    }
    public void setWeight(float weight) {

        editor.putFloat(KEY_USER_WEIGHT, weight);
        // commit changes
        editor.commit();


    }
    public void setHeight(float height) {

        editor.putFloat(KEY_USER_HEIGHT, height);
        // commit changes
        editor.commit();


    }
    public void setAge(int age) {

        editor.putInt(KEY_USER_AGE, Integer.parseInt(age));
        // commit changes
        editor.commit();


    }





    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}