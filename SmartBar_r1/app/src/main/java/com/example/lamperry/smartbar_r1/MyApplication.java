package com.example.lamperry.smartbar_r1;

import android.app.Application;

import java.util.ArrayList;


public class MyApplication extends Application {
    boolean loggedIn = false;
    ArrayList<String> usernameDB = new ArrayList<>();
    ArrayList<String> passwordDB = new ArrayList<>();

    public ArrayList<String> getUsernameDB() {
        return usernameDB;
    }

    public ArrayList<String> getPasswordDB() {
        return passwordDB;
    }

    public void setLoggedIn(boolean setLogIn) {
        loggedIn = setLogIn;
    }

    public boolean getLoggedIn() {
        return loggedIn;
    }
}
