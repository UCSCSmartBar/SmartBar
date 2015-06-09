package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class Container_Screen extends Activity  {

    Inventory INV = new Inventory();
    String ContainerString;
    Button curButt;
    Button preButt;

    boolean toggle = false;

    String DrinkLibString;

    String PREFRENCES_NAME = "PRICE_PREFERENCES";

    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();

    int SelectedContainer = -1;
    //Pickers
    NumberPicker np;    //spirit
    NumberPicker np2;   //brand
    NumberPicker np3;   //volume
    NumberPicker np4;   //price
    NumberPicker np5;   //carbonated
    NumberPicker np6;
    //Picker Values
    String Libs[];
    String Vols[];
    String[] dArray = new String[100];
    String[] cArray = new String[100];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container__screen);
         new Timer().schedule(HideTask,1,100);
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch(NullPointerException n){
            n.printStackTrace();
        }
            //myEditText.clearFocus();


        SetPickers();
        //GetContainersFromMemory();Actually going to get containers from server...
        GetContainerPriceFromMemory();

        new Thread(UpdateContainerContents).start();

        GetContainersFromMemory();
        new Thread(UpdateContainerContents).start();

    }



    public void GetContainersFromMemory(){

        SharedPreferences settings = getSharedPreferences(PREFRENCES_NAME,Context.MODE_PRIVATE);

        String IString = settings.getString("inv","0");
        Log.d("CS_M", "From Mem" + IString);
        SystemCodeParser.DecodeAccessoryMessage(IString);

    }

    /**
     * Adds the Selected item in the pickers to the inventory.
     * @param view
     */
    public void ReplaceContainer(View view){
        //Obtain selected values
        int a = np.getValue();  //Spirit
        int b = np2.getValue(); //brand
        int c = np3.getValue(); //Volume
        int d = np4.getValue(); //price
        int e = np5.getValue(); //carbonated

        //convert to full code & Stringify values
        String Type   = Libs[a];
        String Brand  = String.valueOf(b);
        String maxVol = Vols[c];
        String price =  dArray[d];


        //Sets the price of containers
        Type = DrinkStrings.StringToCode(Type);
        int carb = e;
        //Toast.makeText(getApplicationContext(),"Carbed:"+carb,Toast.LENGTH_LONG).show();

        float vol;
        float cost;
        try {
            vol = Float.parseFloat(maxVol);
            cost = Float.parseFloat(price);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return;
        }

        try {
            //Adds/Updates new LCO to inventory
            Log.d("CS_RC", "Prev Con:" + INV.getContainer(SelectedContainer).PrintContainer());
            INV.AddToInventory(SelectedContainer, Type, Brand, carb, vol, vol, cost);
            ContainerString = INV.getContainer(SelectedContainer).PrintContainer();
            Log.d("CS_RC", "New Con:" + ContainerString);
            TextView tView = (TextView) findViewById(R.id.cont_tview);
            tView.setText(ContainerString);
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }

        UpdateContainerContents.run();
        StoreContainersInMemory();
        StorePricesInMemory();

    }

    /**
     * Stores the replaced container in memory by storing the inventory string in Shared Preferences.
     * Draws information from Inventory class rather than Conatiner Screen.
     */
    public void StoreContainersInMemory( ){
        //Updating contents in memory
        String IString = "";
        for(String s :INV.SerialInventory()){
            if(s!=null) {
                IString += s;
            }
        }
        //Log.d("CS_M",IString);
        SharedPreferences settings = getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE);
        settings.edit().putString("inv",IString).apply();
        Log.d("CS_M", "Stored Inventory " + settings.getString("inv", "0"));

    }

    /**
     * Since the prices are not part of the inventory string, they are stored here locally
     * Draws information from Inventory class rather than Conatiner Screen.
     */
    public void StorePricesInMemory( ){
        SharedPreferences settings = getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE);
        for(int i = 0; i < INV.GetNumberOfContainers();i++){
            String conNum = Integer.toString(i+1);
            String price = INV.getContainer(i + 1).getPricePerOzStr();
            Log.d("CS_SPIM","Storing "+price+" for container:"+conNum);
            settings.edit().putString(conNum,price).apply();
            Log.d("CS_SPIM", "Stored " + settings.getString(conNum,"0.0"));
        }
    }

    /**
     * The dual of Store Prices in Memory, called within onCreate().
     */
    public void GetContainerPriceFromMemory(){

        SharedPreferences settings = getSharedPreferences(PREFRENCES_NAME,Context.MODE_PRIVATE);
        for(int i = 0;i < INV.GetNumberOfContainers();i++){
            String conNum = Integer.toString(i+1);
            String priceString = settings.getString(conNum, "0.0");
            Log.d("CS_$", "Price in Container " + (i + 1) + "\t" + priceString);
            INV.getContainer(i+1).setPricePerOz(Float.parseFloat(priceString));
        }

    }



    /**
     * Sends the Inventory to the Raspberry Pi by looping getting a serialized version of the inventory
     * and sending it container by container in the format $IV@[Container_Num],[Sprirt],[Brand],[CurVol],[MaxVol]
     * @param view
     */
    public void SendInventory(View view){
        TextView tView = (TextView) findViewById(R.id.cont_tview);
        //tView.setText(INV.SerialInventory());
        //CS.writeString(INV.SerialInventory());

        for(int i =1; i<= INV.GetNumberOfContainers(); i++){

            String s = INV.getContainer(i).SerialContainer();
            Log.d("CS_I", "Serial Cont:" + s);
        }

        String a[] = INV.SerialInventory();
        CommStream PiComm = new CommStream();
        CommStream.writeString("$IV,START");
        String Loga = "";
        for(String temp:a){
            if(temp !=null) {
                Loga += temp;
                CommStream.writeString(temp);
            }
        }
        Log.d("CS_I","Serial Cont:"+Loga);

        CommStream.writeString("$IV,END");
        Sound.playINVUpdated(getApplicationContext());

    }

    public void BackToMain(View view){
        finish();
    }


    //Forceably pours one shot worth of liquid
    public void PurgeContainer(View v){
        if(SelectedContainer < 0 ){return;}
        Log.d("CS_PC", "Purging Container " + SelectedContainer);


        final Dialog dialog = new Dialog(new ContextThemeWrapper(Container_Screen.this, R.style.SmartUIDialog));
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        dialog.setContentView(R.layout.dialog_prime);
        //dialog.setTitle("Do You Have an Account with Smart Bar?");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff444444")));
        dialog.findViewById(R.id.prime_back
        ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.prime_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CommStream.writeString("$DS,$CV," + (SelectedContainer - 1) + ",1.5");


            }
        });
        TextView t = (TextView) dialog.findViewById(R.id.prime_tview);
        t.setText("Press Start Purge container");
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);


    }

    public void PrimeContainer(View v){

        if(SelectedContainer < 0 ){return;}
        Log.d("CS_PC","Prime Container "+SelectedContainer);

        final Dialog dialog = new Dialog(new ContextThemeWrapper(Container_Screen.this, R.style.SmartUIDialog));
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        dialog.setContentView(R.layout.dialog_prime);
        //dialog.setTitle("Do You Have an Account with Smart Bar?");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff444444")));
        dialog.findViewById(R.id.prime_back
        ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.prime_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    CommStream.writeString("$DS,$OV,"+(SelectedContainer-1)+",2");
            }
        });
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    }

    public void TestValves(View v){

        CommStream.writeString("$DS,$TV");
    }

    //Server Related Functions
    public void UpdatePrices(){


        SharedPreferences settings = getSharedPreferences(PREFRENCES_NAME,Context.MODE_PRIVATE);
        //Updates the old prices from app close;
        Inventory INV = new Inventory();
        for(int i = 0;i < INV.GetNumberOfContainers();i++){
            String conNum = Integer.toString(i+1);
            String priceString = settings.getString(conNum, "0.0");
            Log.d("CS_$","Price in Container "+ (i+1)+"\t"+priceString);
            INV.getContainer(i+1).setPricePerOz(Float.parseFloat(priceString));
        }



        String[] parsedArray = DrinkLibString.split("#");
        String drinkNameString = parsedArray[0];
        String drinkRecipeString = parsedArray[1];
        int drinkCount = 0;
        String[] tempName, tempRecipe;


        for (int i = 0; i < drinkNameString.length(); i++) {
            if (drinkNameString.charAt(i) == '%') {
                drinkCount++;

            }
        }
        tempName = drinkNameString.split("%");
        tempRecipe = drinkRecipeString.split("%");
        for (int k = 0; k < drinkCount; k++) {
            Log.d("MM_UP","Name:"+ tempName[k]);
            Log.d("MM_UP", "In Recipe: "+tempRecipe[k]);
            double price = DrinkOrder.ParseDrinkForPrice(tempRecipe[k]);
            Log.d("MM_UP","Parse Price:" +price);
        }


    }



    /****************Spinner/Visual Related Functions**********************************************/
    /***********************************************************************************************/
    /**
     * Populates and formats the pickers
     */
    public void SetPickers(){

        Libs = new String[DrinkStrings.DrinkLibraryNames.size()];
        int i = 0;
        for(String key: DrinkStrings.DrinkLibraryNames.keySet() ){
            Libs[i++] = DrinkStrings.DrinkLibraryNames.get(key);
            Log.d("CS_SP",key+":"+Libs[i-1]);
        }
        np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setDisplayedValues(Libs);
        np.setMinValue(0);
        np.setWrapSelectorWheel(true);
        np.setMaxValue(Libs.length-1);
        setNumberPickerTextColor(np,Color.parseColor("#ffffff"));


        np2 = (NumberPicker) findViewById(R.id.numberPicker2);
        np2.setMinValue(0);
        np2.setMaxValue(4);
        setNumberPickerTextColor(np2,Color.parseColor("#ffffff"));

        //
        np3 = (NumberPicker) findViewById(R.id.numberPicker3);
        np3.setMinValue(0);

        Vols = new String[]{"0.0","10.0","15.0","20.0","25.36","33.9","59.3","100.0"};
        np3.setMaxValue(Vols.length - 1);
        np3.setDisplayedValues(Vols);
        setNumberPickerTextColor(np3,Color.parseColor("#ffffff"));

        //np4 is the dollar amount
        np4 = (NumberPicker) findViewById(R.id.numberPicker4);
        np4.setMinValue(0);

        //np6 is the cent amount


        //
        Log.d("CS_SP","Starting loop");

        for(int j = 0; j < cArray.length;j++){
            dArray[j] = String.format("%.2f",j*.25);
            Log.d("CS_SP",dArray[j]+" "+cArray[j]);
        }
        //
        np4.setDisplayedValues(dArray);
        np4.setMaxValue(dArray.length - 1);
        setNumberPickerTextColor(np4,Color.parseColor("#ffffff"));

        //np5 is the carbonation selector
        np5 = (NumberPicker) findViewById(R.id.numberPicker5);
        np5.setDisplayedValues(new String[]{"Liquor","Mixer"});
        setNumberPickerTextColor(np5, Color.parseColor("#ffffff"));
        np5.setMinValue(0);
        np5.setMaxValue(1);

    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("snptc", e);
                }
                catch(IllegalAccessException e){
                    Log.w("snptc", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("snptc", e);
                }
            }
        }
        return false;
    }

    /**
     * Displays selected container information and jumps to the modifier
     * @param view
     */
    public void onContainerClick(View view) {

        /**changes the buttons color***/
        preButt = curButt;
        curButt = (Button) view;
        if(preButt!=null){
            if(preButt == findViewById(R.id.con6_button) || preButt == findViewById(R.id.con7_button)){
                preButt.setBackground(getResources().getDrawable(R.drawable.wcont_butt));
                preButt.setTextColor(getResources().getColor(R.color.white));
            }else{
                preButt.setBackground(getResources().getDrawable(R.drawable.cont_butt));
                preButt.setTextColor(getResources().getColor(R.color.white));
            }
        }


        if(curButt == findViewById(R.id.con6_button) || curButt == findViewById(R.id.con7_button)){
            curButt.setBackground(getResources().getDrawable(R.drawable.wcont_butt_high));
            curButt.setTextColor(getResources().getColor(R.color.black));
        }else {
            curButt.setBackground(getResources().getDrawable(R.drawable.cont_butt_high));
            curButt.setTextColor(getResources().getColor(R.color.black));
        }

        //Opens up corresponding container with information
        SelectedContainer = findContainerByID(view);

        //Toast.makeText(getApplicationContext(), ContainerString, Toast.LENGTH_LONG).show();

        //Jumps to the corresponding spinner of the selected container
        String s = INV.getContainer(SelectedContainer).Spirit;
        String brand = INV.getContainer(SelectedContainer).Brand;
        double price = INV.getContainer(SelectedContainer).getPricePerOz();
        int carbed = (INV.getContainer(SelectedContainer).isCarbonated())? 1 : 0;
        String vol =  String.format("%.2f", (INV.getContainer(SelectedContainer).getMaxVolume()));
        String pString = String.format("%.2f",price);

        //Snaps to info
        try{
            np2.setValue(Integer.parseInt(brand));
        }catch(NumberFormatException nfe){
            nfe.printStackTrace();
        }

        Log.d("CS","Spirit"+s);
        String b = DrinkStrings.DrinkLibraryNames.get(s);
        Log.d("CS","Bname:"+b);

        int index = SearchLibs(b);
        if(index>-1){
            np.setValue(index);
        }
        //Sets the price of container
        index = SearchPrice(pString);
        np4.setValue(index);
        np3.setValue(SearchArray(vol, Vols));
        np5.setValue(carbed);


    }

    /**
     * Visually Update the container and its name
     * //TODO Show liquid rising/falling
     */
    Runnable UpdateContainerContents = new Runnable() {
        @Override
        public void run() {

            for(int i = R.id.con1_button, j =1; i <= R.id.con18_button; i++, j++) {

                //Log.d("CS",j+"th ID is :"+i);
                Button button = (Button) findViewById(i);
                try {
                    button.setTextSize(20);
                    button.setText("Container "+j+"\n"+INV.getContainer(j).PrintContainer());
                }catch(NullPointerException npe){
                    npe.printStackTrace();
                }
            }
        }
    };

    /********Searching through Container functions*************************************************/
    /**********************************************************************************************/
    //Returns container number, and replaces the text view text
    public int findContainerByID(View view) {
        switch (view.getId()) {
            case R.id.con1_button:
                ContainerString = INV.getContainer(1).PrintContainer();
                //button.setBackground();
                SelectedContainer = 1;
                break;
            case R.id.con2_button:
                ContainerString = INV.getContainer(2).PrintContainer();
                SelectedContainer = 2;
                break;
            case R.id.con3_button:
                ContainerString = INV.getContainer(3).PrintContainer();
                SelectedContainer = 3;
                break;
            case R.id.con4_button:
                ContainerString = INV.getContainer(4).PrintContainer();
                SelectedContainer = 4;
                break;
            case R.id.con5_button:
                ContainerString = INV.getContainer(5).PrintContainer();
                SelectedContainer = 5;
                break;
            case R.id.con6_button:
                ContainerString = INV.getContainer(6).PrintContainer();
                SelectedContainer = 6;
                break;
            case R.id.con7_button:
                ContainerString = INV.getContainer(7).PrintContainer();
                SelectedContainer = 7;
                break;
            case R.id.con8_button:
                ContainerString = INV.getContainer(8).PrintContainer();
                SelectedContainer = 8;
                break;
            case R.id.con9_button:
                ContainerString = INV.getContainer(9).PrintContainer();
                SelectedContainer = 9;
                break;
            case R.id.con10_button:
                ContainerString = INV.getContainer(10).PrintContainer();
                SelectedContainer = 10;
                break;
            case R.id.con11_button:
                ContainerString = INV.getContainer(11).PrintContainer();
                SelectedContainer = 11;
                break;
            case R.id.con12_button:
                ContainerString = INV.getContainer(12).PrintContainer();
                SelectedContainer = 12;
                break;
            case R.id.con13_button:
                ContainerString = INV.getContainer(13).PrintContainer();
                SelectedContainer = 13;
                break;
            case R.id.con14_button:
                ContainerString = INV.getContainer(14).PrintContainer();
                SelectedContainer = 14;
                break;
            case R.id.con15_button:
                ContainerString = INV.getContainer(15).PrintContainer();
                SelectedContainer = 15;
                break;
            case R.id.con16_button:
                ContainerString = INV.getContainer(16).PrintContainer();
                SelectedContainer = 16;
                break;
            case R.id.con17_button:
                ContainerString = INV.getContainer(17).PrintContainer();
                SelectedContainer = 17;
                break;
            case R.id.con18_button:
                ContainerString = INV.getContainer(18).PrintContainer();
                SelectedContainer = 18;
                break;
        }
        TextView tView = (TextView) findViewById(R.id.cont_tview);
        tView.setText(ContainerString);
        return SelectedContainer;
    }

    public int SearchLibs(String s){
        if(s==null){return -1;}
        for(int i =0; i< Libs.length;i++){
            Log.d("CS_S","S:"+s+" Libs "+Libs[i]);
            if(Libs[i] == null){return -1;}
            if(Libs[i].equals(s)){
                return i;
            }
        }
        return -1;
    }

    public int SearchPrice(String s){
        if(s==null){return -1;}
        for(int i =0; i< dArray.length;i++){
            Log.d("CS_S","S:"+s+" $"+dArray[i]);
            if(dArray[i] == null){return -1;}
            if(dArray[i].equals(s)){
                return i;
            }
        }
        return -1;
    }

    public int SearchArray(String s, String[] S){
        if(s==null || S==null){return -1;}
        int i=0;
        for(String a : S){
            if(s.equals(a)){ return i;}
            i++;
        }
        return -1;
    }



    /**************************System Functions*********************************/
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
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    TimerTask HideTask = new TimerTask() {
        @Override
        public void run(){
            Container_Screen.this.runOnUiThread(new Runnable() {
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
        getMenuInflater().inflate(R.menu.menu_container__screen, menu);
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
