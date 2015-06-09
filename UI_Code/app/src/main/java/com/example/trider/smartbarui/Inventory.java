package com.example.trider.smartbarui;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
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

import java.lang.reflect.Field;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import java.util.Iterator;
import java.util.logging.Level;


/**
 * @Created by trider on 3/1/2015.
 * @about The abstract class that contains information about what is currently in the Smart Bar.
 * The commands to call these functions will either be UI buttons from the System Status Menus,
 * or via from the Pi because of the SystemCodeParser.
 *
 */
public class Inventory {
    //Maximum volume of a container in oz. and other common measurements
    private static final double MAX_VOL_HANDLE = 100.0; //[oz]
    private static final double MAX_VOL_FIFTH = 25.36; //[oz]
    private static final double VOL_SHOT = 1.5;        //[oz]



    //Simple Identifiers for quick level references.
    private static final String FULL = "FULL";
    private static final String LOW = "LOW";
    private static final String EMPTY = "EMPTY";

    private static final String INV_TAG = "INV";

    private static final String UNDEF = "Empty Con";
    //Current Max number of containers
    private static int MAX_CONTAINERS = 18;
    private static int NUM_CONTAINERS = 18;

    private static int NUM_MIXERS = 0;
    private static int NUM_LIQUORS = 0;

    private static int TimesInitialized = 0;
    //Creates one Inventory for the whole program.
    public static LiquidContainerObj[] Containers;

    private static Boolean initialized = false;
    public static Boolean pricesSet = false;

    //public double Trim(double num){
//        int t = (int) num*100;
//        Log.d("Trim","In:"+num+",t:"+t+",out:"+(t/100));
//        return t/100;
//
//    }



    /**
     * @Datatype: InnerClass for Liquid Container Object.
     */
    public class LiquidContainerObj{
        //String identifiers for individual contents
        String Spirit;
        String Brand;
        //The quick identifier
        String LevelStatus;
        //For Carbonation
        int Carbonated = 0;
        //Liquid level quick references
        double MaxVolume = MAX_VOL_HANDLE;
        //Assuming empty container
        double CurVolume = 0;
        //Price of liquor
        double PricePerOz = 0.00; //[$US]
        //Defining a new Liquor Container Obj;

        /**
         * Creates a new container object.
         * @param spirit What type of liquor/mixer is being added e.g. Whiskey, Gatorade, Juice
         * @param brand What is the actual brand/type e.g. Johnny Walker, Fruit Punch, Lime Juice
         * @param maxVolume The maximum volume of the container itself [oz]
         * @param curVolume What the volume of the container is currently. [oz]
         */
        LiquidContainerObj(String spirit, String brand,  int carbonated, double curVolume, double maxVolume, double pricePerOz){
            Log.d("LCO","Creating new LCO with "+ spirit+":"+carbonated+":" + brand+":" + maxVolume+":"
                    + curVolume);
            Spirit = spirit;
            Brand = brand;
            Carbonated = carbonated;

            if(maxVolume > MAX_VOL_HANDLE){
                MaxVolume = MAX_VOL_HANDLE;
            }else if(maxVolume < 0.0) {
                LevelStatus = EMPTY;
                return;
            }else{
                MaxVolume = maxVolume;
            }
            this.setPricePerOz(pricePerOz);
            this.setCurVolume(curVolume);
        }
        //Overloaded constructors
        LiquidContainerObj(String spirit, String brand, double maxVolume){
            Log.d("LCO","Creating new LCO with "+ spirit+":" + brand+":" + maxVolume+":");
            Spirit = spirit;
            Brand = brand;

            if(maxVolume > MAX_VOL_HANDLE){
                MaxVolume = MAX_VOL_HANDLE;
            }else if(maxVolume < 0.0) {
                LevelStatus = EMPTY;
                return;
            }else{
                MaxVolume = maxVolume;
            }

            LevelStatus = FULL;
        }

        LiquidContainerObj(String spirit, String brand){
            Log.d("LCO","Creating new LCO with "+ spirit+":" + brand+":");
            Spirit = spirit;
            Brand = brand;
            LevelStatus = EMPTY;
        }

        LiquidContainerObj(){
            Log.d("LCO","Creating new empty LCO");
            Spirit = UNDEF;
            Brand = UNDEF;
            LevelStatus = EMPTY;
        }

        /**
         *
         * @return CurVolume Current volume in container in [oz]
         */
        public double getCurVolume(){
            return CurVolume;
        }

        /**
         *
         * @return CurVolume Current volume in container in [oz]
         */
        public double getMaxVolume(){
            return MaxVolume;
        }

        /**
         * @return LevelStatus quick reference for container level
         */
        public String getLevelStatus(){
            return LevelStatus;
        }

        /**
         *
         */
        public boolean isCarbonated(){
            if(Carbonated==1){return true;}
            return false;
        }

         public boolean isEmpty(){
                 if(Brand.equals(UNDEF)){
                     return true;
                 }else{
                     return false;
                 }
             }

         /**
         * @return PricePerOz returns the price of what is in the liquor
         */
        public double getPricePerOz(){ return PricePerOz; }

        /**
         *
         * @return Returns a $$.CC value of the price/oz of the container
         */
        public String getPricePerOzStr(){ return String.format("%.2f",PricePerOz);}
        /**
         * For when updating with raspberry pi to sync levels
         * @param vol sets current volume of the container
         */
        public void setCurVolume(double vol){
            if(vol > MAX_VOL_HANDLE || vol < 0){
                return;
            }
            CurVolume = vol;

            if(2*CurVolume > MaxVolume){
                LevelStatus = FULL;
            }else if(CurVolume > .25*MaxVolume){
                LevelStatus = LOW;
            }else{
                LevelStatus = EMPTY;
            }
        }

        /**
         * Set Price per oz of liquor
         * @param price in $Dollar.Cent form
         */
        public void setPricePerOz(double price){
            if(price > 0.0){
                PricePerOz = price;
            }
        }

        //@TODO Limit the string length of each block
        /**
         * Prints the object as a row
         * @return A User readable string about the contents of a certain container.
         */
        public String PrintContainer(){
            String m = (isCarbonated()) ? "Mixer":"Liquor";
            return String.format("%2s | %2s | %.3f / %.3f  | %2s  |%.2f | %s", Spirit, Brand, getCurVolume(),
                    getMaxVolume(), getLevelStatus(),getPricePerOz(),m);
        }

        /**
         * Converts the string to the corresponding container string
         * @return The SerialContainer
         */
        public String SerialContainer(){

            String SerCont;
            if(Spirit.equals(UNDEF)){return null;}
            SerCont = String.format("%s,%s,%.2f,%.2f",Spirit,
                    DrinkStrings.BrandToCode(Brand),getCurVolume(),getMaxVolume());
            Log.d("LCO","Serializing Container:"+SerCont);
            return SerCont;
        }


    }
    /**************************End of InnerClass***************************************************/




    /**
     * Defines a new inventory based on the number of containers
     */
    Inventory() {
        if(initialized){return;}
        Containers = new LiquidContainerObj[NUM_CONTAINERS];
        TimesInitialized++;
        for (int i = 0; i < NUM_CONTAINERS; i++) {
            Containers[i] = new LiquidContainerObj();
        }
        initialized = true;
    }



    public void SetNumberOfContainers(int NOC){
        if(NOC < 1 || NOC > 20){return;}
        NUM_CONTAINERS = NOC;
    }
    public int GetNumberOfContainers(){
//        int num = 0;
//        for(int i = 0; i< MAX_CONTAINERS; i++){
//            Log.d(INV_TAG,"Container Brand:"+getContainer(i+1).Brand);
//            if(getContainer(i+1).Brand == null || getContainer(i+1).Brand.equals(UNDEF)){
//
//            }else{
//                num++;
//                Log.d(INV_TAG,"+:Container Brand:"+getContainer(i+1).Brand);
//            }
//
//        }
        return NUM_CONTAINERS;

    }
    /**
     * Used for setting up initial inventory, or for complete replacements
     * @param ContainerNum Which container is being added.
     * @param newContainer The contents of the container
     */
    public void AddToInventory(int ContainerNum,LiquidContainerObj newContainer){
        //Guard condition for swapping containers
        if(ContainerNum < 1 || ContainerNum > NUM_CONTAINERS) {
            return;
        }
        //If given an empty container, do not add it.
        if(newContainer == null) {
            return;
        }
        //Replaces old container if any is in there.
        Containers[ContainerNum-1] = newContainer;
    }

    /**
     * Overloaded constructor for containers, for when the Bartender/Owner sets his own custom
     * containers and liquids
     * @param ContainerNum Which container is being added.
     * @param spirit What type of liquor/mixer is being added e.g. Whiskey, Gatorade, Juice
     * @param brand What is the actual brand/type e.g. Johnny Walker, Fruit Punch, Lime Juice
     * @param maxVolume The maximum volume of the container being added [oz]
     */
    public void AddToInventory(int ContainerNum,String spirit, String brand,int carbonated, double curVolume,
                               double maxVolume, double pricePerOz ){
        //Guard condition for swapping containers
        if(ContainerNum < 1 || ContainerNum > NUM_CONTAINERS) {
            return;
        }


        Log.d(INV_TAG,"New Liquid Container Object with :"+"["+ContainerNum+"]["+spirit+"]["+brand
                +"]["+Integer.toString(carbonated)+"]["+Float.toString((float)curVolume)+"]/["+Float.toString((float)maxVolume)+"]");

        Containers[ContainerNum-1] = new LiquidContainerObj(spirit,brand,carbonated,curVolume,maxVolume,pricePerOz);




    }

    /**
     * Removes a container from the inventory and replaces it with an empty one.
     * @param ContainerNum Which container is being removed
     */
    public void RemoveFromInventory(int ContainerNum){
        if(ContainerNum < 1 || ContainerNum > NUM_CONTAINERS) {
            return;
        }
        //Creating a new container essentially empties it.
         Containers[ContainerNum-1] = new LiquidContainerObj();
    }

    public int NumOfMixers(){
        int nom = 0;
        for(int i = 0; i < NUM_CONTAINERS; i++){
            LiquidContainerObj LCO = getContainer(i+1);
            if(LCO.isCarbonated() && !LCO.Brand.equals(UNDEF)){
                nom++;
                Log.d("LCO_#", "LCO["+i+"] "+LCO.PrintContainer()+ " is a mixer. NOM is now:"+nom);

            }
        }
        return nom;
    }
    public int NumOfLiquors(){
        int nol = 0;
        for(int i = 0; i < NUM_CONTAINERS; i++){
            LiquidContainerObj LCO = getContainer(i+1);
            if(!LCO.isCarbonated()&& !LCO.Brand.equals(UNDEF)){

                nol++;
                Log.d("LCO_#", "LCO["+i+"] "+LCO.PrintContainer()+ " is a liquor. NOL is now:"+nol);
            }
        }
        return nol;
    }
    /**
     * Updates the inventory after a drink has been placed.
     * @param ContainerNum Which container got poured/filled
     * @param vol The newVolume of the container.
     * @return The current inventory after update or error reason.
     */
    public String UpdateInventory(int ContainerNum, double vol){

        if(ContainerNum < 0 || ContainerNum > NUM_CONTAINERS){
            return "Invalid Container Number["+ContainerNum+"]";
        }else if(vol > MAX_VOL_HANDLE || vol < 0){
            return "Invalid volume amount["+vol+"]";
        }
        Containers[ContainerNum-1].setCurVolume(vol);
        return "Update Success|"+Containers[ContainerNum-1].PrintContainer();

    }

    public LiquidContainerObj getContainer(int conNum){
        if(conNum < 1 || conNum > NUM_CONTAINERS){
            Log.d(INV_TAG,"Invalid container");
            return null;
        }else{
            return Containers[conNum-1];
        }

    }


    /**
     * Creates a tabled log of what is currently in the inventory
     * @return A User readable table of the current inventory.
     */
    public String PrintInventory() {
        //Header
        String Table = "Cont# | Spirit | Brand | CurVol | MaxVolume | Status | Price\n";

        try {
            //Row Information
            for (int i = 0; i < NUM_CONTAINERS; i++) {
                Table += ((i+1) + "|" + Containers[i].PrintContainer() + "\n");
            }
        } catch (StringIndexOutOfBoundsException s) {
            Log.d(INV_TAG, "Number of Container Errors:");
            s.printStackTrace();
            return "Error";
        } catch (NullPointerException npe){
            Log.d(INV_TAG, "Unknown NPE");
            npe.printStackTrace();
            return "Error";
        }
        return Table;
    }





    /**
     * Creates the serial Inventory Data Packet
     * @return The serial Inventory Data Packet.
     */
    public String[] SerialInventory(){
        String SerialCmd[] = new String[30];

        int j=1;
        int nol=0;
        int nom = 0;
        try {
            //Liquors first
            for (int i = 0; i < NUM_CONTAINERS; i++) {
                if(!Containers[i].isCarbonated() && !Containers[i].isEmpty()) {
                    SerialCmd[j++] = "$IV,@" + (i) + "," + Containers[i].SerialContainer();
                    Log.d(INV_TAG,"Liqour Container["+i+"]:"+Containers[i].SerialContainer());
                    nol++;
                }
            }
            //Mixers next
            for (int i = 0; i < NUM_CONTAINERS; i++) {
                if(Containers[i].isCarbonated() && !Containers[i].isEmpty()) {
                    SerialCmd[j++] = "$IV,@" + (i) + "," + Containers[i].SerialContainer();
                    Log.d(INV_TAG,"Mixer Container["+i+"]:"+Containers[i].SerialContainer());
                    nom++;
                }
            }
            SerialCmd[0]= String.format("$IV,%d,%d",nol,nom);


            Log.d(INV_TAG,"SerInvStr:");
            for(String s:SerialCmd){
                if(s!= null)
                    Log.d(INV_TAG,s);
            }
            Log.d(INV_TAG,"String is:"+SerialCmd.length+" Long");
        } catch (StringIndexOutOfBoundsException s) {
            Log.d(INV_TAG, "Number of Container Errors:");
            s.printStackTrace();
            return null;
        } catch (NullPointerException npe){
            Log.d(INV_TAG, "Unknown NPE");
            npe.printStackTrace();
            return null;
        }
        String msg = "";
        for(String s: SerialCmd) {
            if (s != null) msg += s;
        }
        Log.d("INV","Full String: "+msg);
        return SerialCmd;

    }


    /**
     * Searches the inventory for certain liquor, needs both identifier and brand name.
     * @param spirit What type of liquor/mixer is being added e.g. Whiskey, Gatorade, Juice
     * @param brand What is the actual brand/type e.g. Johnny Walker, Fruit Punch, Lime Juice
     * @return
     */
    public LiquidContainerObj searchInventory(String spirit, String brand){
        if(spirit == null || brand == null){return null;}

        for(int i =1; i <= NUM_CONTAINERS; i++){
            LiquidContainerObj temp = getContainer(i);
            if(temp == null){continue;}
            Log.d("INV","In sprirt "+spirit+"In brand "+brand);
            Log.d("INV","Container spirit "+temp.Spirit+"Container brand "+temp.Brand);
            if(temp.Spirit.equalsIgnoreCase(spirit) && temp.Brand.equalsIgnoreCase(brand)){
                return temp;
            }
        }
        return null;


    }
}
