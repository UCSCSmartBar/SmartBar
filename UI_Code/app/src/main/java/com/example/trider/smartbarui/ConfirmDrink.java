package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ConfirmDrink extends Activity {

    private static final String DELETE_URL = "http://www.ucscsmartbar.com/delQ.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();




    CommStream PiComm = new CommStream();
    DrinkOrder Do = new DrinkOrder();

    TextView DrinkListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_drink);
        hideSystemUI();
        DrinkListing = (TextView) findViewById(R.id.drink_listing);

        if (Do.getCurrentDrinkOrder() != null) {
            DrinkListing.setText(Do.DecodeString(Do.getCurrentDrinkOrder()));
        }
    }


    public void No(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);

        //The view being uploaded is formatted in the fragment_change_liquor_inv.xml.xml file is in the text
        //box area
        View v = inflater.inflate(R.layout.fragment_change_liquor_inv, null);
        TextView tView = (TextView) findViewById(R.id.confmsg);

        //Print the drink obtained
        if(Do.getCurrentDrinkOrder()!=null){
            tView.append(Do.DecodeString(Do.getCurrentDrinkOrder()));
        }
        //Builds the actual pop up with custom style
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.SmartUIDialog))
                .setTitle("Are you sure you didn't order this Drink?")
                .setMessage("Scroll for current com.example.trider.smartbarui.Inventory")
                .setView(v)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing

                        startActivity(new Intent(ConfirmDrink.this, IdleMenu.class));
                    }
                })
                        //.setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }


    public void PourDrink(View view){
        PiComm.writeString("$DO,"+Do.getCurrentDrinkOrder());
        startActivity(new Intent(this,IdleMenu.class));
    }



    class AttemptDeleteQ extends AsyncTask<String, String, String> {
        int success;
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("DelQ", "Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("DelQ", "Mid-Execute");
                Log.d("DelQ", "starting");
                // getting product details by making HTTP request

                String PinToDelete = "11100002108";
                PinToDelete = new DrinkOrder().InUserPinString;
                Log.d("DelQ","PinToDelete:" + PinToDelete);
                Log.d("DelQ","Pin getting Posted:" + PinToDelete);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", PinToDelete));
                Log.d("DelQ","HTTP getting Posted:" + DELETE_URL);
                JSONObject json = jsonParser.makeHttpRequest(DELETE_URL, "POST", params);

                // check your log for json response
                Log.d("DelQ", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("DelQ", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("DelQ","Failure with :"+ json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted

            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("DelQ","Returned URL:"+ file_url);

            } else {
                Toast.makeText(ConfirmDrink.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }




    /**
     * System Level Functions*
     */
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm_drink, menu);
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