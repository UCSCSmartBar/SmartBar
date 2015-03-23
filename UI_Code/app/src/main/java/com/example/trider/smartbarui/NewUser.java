package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class NewUser extends Activity {

    public DrinkOrder testDrink;
    private CommStream PiComm = new CommStream();
    JSONParser jsonParser = new JSONParser();

    //Views
    ProgressBar pBar;
    Toast toast;
    EditText eText;
    private ProgressDialog pDialog;

    //Variables
    boolean searching = false;
    boolean searchFailure = true;
    String pinString="";
    String IncomingString;
    String OutMessage;
    String[] ParsedString;


    //Login tags
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/getDrink.php";
    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";



    //Listens to Pi
    boolean isActive = true;
    class ListenTask extends TimerTask {
        @Override
        public void run(){
            NewUser.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        /**
                         * @subTimeTask
                         */
                        String t = CommStream.ReadBuffer();
                        if(t!=null) {
                            TextView tV = (TextView) findViewById(R.id.drinkView);
                            tV.append(t);
                            String rMessage = SystemCodeParser.DecodeAccessoryMessage(t);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        isActive =false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        pBar = (ProgressBar) findViewById(R.id.newUserProgress);
        pBar.setVisibility(View.INVISIBLE);

        hideSystemUI();
        new Timer().scheduleAtFixedRate(HideTask,100,100);
        new Timer().scheduleAtFixedRate(new ListenTask(),100,100);
        TextView tV = (TextView) findViewById(R.id.drinkView);
        tV.setText("CurBuff:"+ CommStream.ReadBuffer()+ "CurQ:"+ DrinkOrder.CurDrinkQueue);
        //Using manual Keyboard
//        EditText eText = (EditText) findViewById(R.id.txtPin);
//
//        eText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(11)});

        //startWatch(10000);
    }


    public void CheckPin(View view){
        Context context = getApplicationContext();


        new AttemptGetDrink().execute();

        //Displays the progress bar so user knows the drink is being looked up
        pBar = (ProgressBar) findViewById(R.id.newUserProgress);
        pBar.setVisibility(View.VISIBLE);
        searching = true;

        //Creates a singleton task that will run in exactly 2000ms after the button is clicked
        new Timer().schedule(new TimerTask() {
            public void run() {
                NewUser.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pBar.setVisibility(View.INVISIBLE);
                        searching = false;
                        Intent intent = new Intent(NewUser.this,NewUserFinger.class);
                        //If nothing came up from search
                        if(searchFailure){
                            return;
                        }
                        //If Found on the Server
                        if(!isInQueue(DrinkOrder.InUserPinString)){
                            Toast.makeText(getApplicationContext(),"You do not have a drink on the Queue",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        intent.putExtra("tString","$FPQ,"+pinString + "$DO,"+IncomingString);
                        //PiComm.writeString("$FPQ,"+pinString);

                        DrinkOrder t = new DrinkOrder();
                        t.DecodeString(IncomingString);

                        t.storeDrinkOrder(IncomingString);
                        DrinkOrder.InUserPinString = pinString;
                        //PiComm.writeString("$DO,"+IncomingString);
                        startActivity(intent);
                    }
                });
            }
        },5000);


    }

    /*Limits the User to only put in 11-digits of numerical  characters*/
//    InputFilter filter = new InputFilter() {
//        public CharSequence filter(CharSequence source, int start, int end,
//                                   Spanned dest, int dstart, int dend) {
//            for (int i = start; i < end; i++) {
//                if (!Character.isDigit(source.charAt(i))) {
//                    return "";
//                }
//            }
//            return null;
//        }
//    };


    class AttemptGetDrink extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewUser.this);
            pDialog.setMessage("Attempting to get drink...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            //pDialog.show();
            Log.d("AGD", "Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            //String userpin = eText.getText().toString();
            String userpin = pinString;
            try {
                Log.d("AGD","Mid-Execute");
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", userpin));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // check your log for json response
                Log.d("Drink retrieve attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Drink found Successful!", json.toString());
                    searchFailure = false;
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(NullPointerException npe){
                npe.printStackTrace();
            }

            return null;

        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null){
                //Toast.makeText(NewUser.this, file_url, Toast.LENGTH_LONG).show();
                IncomingString = file_url;
            }else{
                Toast.makeText(NewUser.this,"Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }

    }


    public void GoToRegister(View view){
        startActivity(new Intent(this,NewUserFinger.class));
    }

    public Boolean isInQueue(String userPin){

        if(DrinkOrder.CurDrinkQueue!=null){
            Log.d("Q","Q is:"+DrinkOrder.CurDrinkQueue);
        }else{return false;}

        String[] tokens = DrinkOrder.CurDrinkQueue.split("[@,]");
        Log.d("Q", "In:"+userPin);
        Log.d("Q", "tokens:"+ tokens);
        for(int i = 0; i < tokens.length; i++){
            Log.d("Q","token "+tokens[i]);
            if(tokens[i].equalsIgnoreCase(userPin)){
                Log.d("Q"," found it equal");
                return true;
            }
        }

        Log.d("Q"," Did not find equal");
        return false;
    }
    /***********System Level Functions*******/
    public void SkipToPickFinger (View view){
        startActivity(new Intent(this,PickUpFinger.class));

    }


//    };



    /***********System Level Functions*******/
    /**Manuel Keyboard**/
    //@TODO Make into a custom fragment
    public void EnterPin(View view){
        switch(view.getId()){
            case R.id.keyOne:
                pinString+="1";
                break;
            case R.id.keyTwo:
                pinString+="2";
                break;
            case R.id.keyThree:
                pinString+="3";
                break;
            case R.id.keyFour:
                pinString+="4";
                break;
            case R.id.keyFive:
                pinString+="5";
                break;
            case R.id.keySix:
                pinString+="6";
                break;
            case R.id.keySeven:
                pinString+="7";
                break;
            case R.id.keyEight:
                pinString+="8";
                break;
            case R.id.keyNine:
                pinString+="9";
                break;
            case R.id.keyZero:
                pinString+="0";
                break;
            case R.id.keyBack:
                if(pinString.length() == 0){return;}
                pinString = pinString.substring(0,pinString.length()-1);
                break;
            case R.id.keyEnter:
                if(pinString.length() < 11){
                    Toast.makeText(getApplicationContext(),"Please Enter Full 11-Digits",Toast.LENGTH_SHORT).show();
                }else if(pinString.length()> 11){
                    Toast.makeText(getApplicationContext(),"Phone Number Entered is Too Long",Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(getApplicationContext(),"Hey... Ok",Toast.LENGTH_SHORT).show();
                    DrinkOrder.InUserPinString = pinString;
                    CheckPin(view);
                }
                break;
        }
        TextView tView = (TextView) findViewById(R.id.enterField);
        tView.setText(pinString);
    }



    public void startWatch(int watch_t) {
        new Timer().schedule(new TimerTask() {
            public void run() {
               startActivity(new Intent(NewUser.this, IdleMenu.class));
            }

        }, watch_t);
    }




    /*System Level Functions*/

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View mDecorView;
        mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    TimerTask HideTask = new TimerTask() {
        @Override
        public void run(){
            NewUser.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}




