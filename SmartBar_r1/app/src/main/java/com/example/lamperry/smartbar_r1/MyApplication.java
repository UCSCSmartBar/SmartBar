package com.example.lamperry.smartbar_r1;

import android.app.Application;

import java.util.ArrayList;
import java.util.Random;


// this class holds all the global variables for the entire app
// namely the particular username/password who is currently logged in
// for now the DB arrays too
public class MyApplication extends Application {
    int pin = 0;
    Random random = new Random();
    boolean loggedIn = false;
    boolean first = true;
    String myUsername;
    String myPassword;
    int myPin;
    ArrayList<String> usernameDB = new ArrayList<>();
    ArrayList<String> passwordDB = new ArrayList<>();
    ArrayList<Integer> uniquePins = new ArrayList<>();

    public ArrayList<String> getUsernameDB() { return usernameDB; }
    public ArrayList<String> getPasswordDB() { return passwordDB; }
    public ArrayList<Integer> getUniquePins() { return uniquePins; }

    public void setMyUsername(String username) { myUsername = username; }
    public void setMyPassword(String password) { myPassword = password; }
    public void setPin(int pin) { myPin = pin; }

    public String getMyUsername() { return myUsername; }
    public String getMyPassword() { return myPassword; }
    public int getPin() { return myPin; }

    public boolean isFirst() { return first; }
    public void notFirst() { first = false; }
    public void setFirst() { first = true; }

    public void setLoggedIn(boolean setLogIn) {
        loggedIn = setLogIn;
    }

    public int addPin() {
        pin = random.nextInt(10000);
        uniquePins.add(pin);
        return pin;
    }
}
