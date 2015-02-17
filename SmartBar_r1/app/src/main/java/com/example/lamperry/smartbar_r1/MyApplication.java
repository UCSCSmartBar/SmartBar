package com.example.lamperry.smartbar_r1;

import android.app.Application;

import java.util.ArrayList;
import java.util.Random;

/*
 * This class holds all the global variables for the entire application. Namely the
 * username/password who is currently logged in and unique pin
 */
// this class holds all the global variables for the entire app
// namely the particular username/password who is currently logged in
// for now the DB arrays too
public class MyApplication extends Application {
    Random random = new Random();
    boolean loggedIn = false;
    boolean first = true;
    String myUsername;
    String myPassword;
    int myPin;

    public void setMyUsername(String username) { myUsername = username; }
    public void setMyPassword(String password) { myPassword = password; }
    public void setPin(int pin) { myPin = pin; }
    public int getPin() { return myPin; }

    public boolean isFirst() { return first; }
    public void notFirst() { first = false; }
    public void setFirst() { first = true; }

    public void setLoggedIn(boolean setLogIn) {
        loggedIn = setLogIn;
    }

    public int addPin() {
        myPin = random.nextInt((int) System.currentTimeMillis() % 1000);
        return myPin;
    }
}
