package com.example.trider.smartbarui;

import android.util.Log;


/**
 * @Created by trider on 3/1/2015.
 * @about The abstract class that contains information about what is currently in the Smart Bar. The
 *         commands to call these functions will either be UI buttons from the System Status Menus, or via
 *         from the Pi because of the SystemCodeParser.
 *
 */
public class Inventory {
    //Maximum volume of a container in oz. and other common measurements
    private static final double MAX_VOL_HANDLE = 59.3; //[oz]
    private static final double MAX_VOL_FIFTH = 25.36; //[oz]
    private static final double VOL_SHOT = 1.5;        //[oz]

    //Simple Identifiers for quick level references.
    private static final String FULL = "FULL";
    private static final String LOW = "LOW";
    private static final String EMPTY = "EMPTY";

    private static final String UNDEF = "Undefined Option";
    //Current Max number of containers
    private static final int NUM_CONTAINERS = 18;

    private static int TimesInitialized = 0;
    //Creates one Inventory for the whole program.
    private static LiquidContainerObj[] Containers;

    private static Boolean initialized = false;

    public double Trim(double num){
        int t = (int) num*100;
        Log.d("Trim","In:"+num+",t:"+t+",out:"+(t/100));
        return t/100;

    }



    /**
     * @Datatype: InnerClass for Liquid Container Object.
     */
    public class LiquidContainerObj{
        //String identifiers for individual contents
        String Spirit;
        String Brand;
        //The quick identifier
        String LevelStatus;
        //Liquid level quick references
        double MaxVolume = MAX_VOL_HANDLE;
        //Assuming empty container
        double CurVolume = 0;

        //Defining a new Liquor Container Obj;

        /**
         * Creates a new container object.
         * @param spirit What type of liquor/mixer is being added e.g. Whiskey, Gatorade, Juice
         * @param brand What is the actual brand/type e.g. Johnny Walker, Fruit Punch, Lime Juice
         * @param maxVolume The maximum volume of the container itself [oz]
         * @param curVolume What the volume of the container is currently. [oz]
         */
        LiquidContainerObj(String spirit, String brand,  double curVolume, double maxVolume){
            Log.d("LCO","Creating new LCO with "+ spirit+":" + brand+":" + maxVolume+":" + curVolume);
            Spirit = spirit;
            Brand = brand;

            if(maxVolume > MAX_VOL_HANDLE){
                MaxVolume = MAX_VOL_HANDLE;
            }else if(maxVolume < 0.0) {
                return;
            }else{
                MaxVolume = maxVolume;
            }
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

        //@TODO Limit the string length of each block
        /**
         * Prints the object as a row
         * @return A User readable string about the contents of a certain container.
         */
        public String PrintContainer(){
            return String.format("%2s | %2s | %.3f / %.3f  | %2s", Spirit, Brand, getCurVolume(), getMaxVolume(), getLevelStatus());
        }

        /**
         * Conversts the string to the corresponding container string
         * @return The SerialContainer
         */
        public String SerialContainer(){

            String SerCont;
            SerCont =String.format("%s,%s,%.3f,%.3f",Spirit,DrinkStrings.BrandToCode(Brand),getCurVolume(),getMaxVolume());

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
    public void AddToInventory(int ContainerNum,String spirit, String brand, double curVolume,double maxVolume ){
        //Guard condition for swapping containers
        if(ContainerNum < 1 || ContainerNum > NUM_CONTAINERS) {
            return;
        }
        Log.d("INV","New Liquid Container Object with :"+"["+ContainerNum+"]["+spirit+"]["+brand+"]["
                        +Float.toString((float)curVolume)+"]//["+Float.toString((float)maxVolume)+"]");
        Containers[ContainerNum-1] = new LiquidContainerObj(spirit,brand,curVolume,maxVolume);
    }

    /**
     * Removes a container from the inventory and replaces it with an empty one.
     * @param ContainerNum Which container is being removed
     */
    public void RemoveFromInventory(int ContainerNum){
        if(ContainerNum < 1 || ContainerNum > NUM_CONTAINERS) {
            return;
        }
         Containers[ContainerNum-1] = new LiquidContainerObj();
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
            Log.d("INV","Invalid container");
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
        String Table = "Cont# | Spirit | Brand | CurVol | MaxVolume | Status\n";

        try {
            //Row Information
            for (int i = 0; i < NUM_CONTAINERS; i++) {
                Table += ((i+1) + "|" + Containers[i].PrintContainer() + "\n");
            }
        } catch (StringIndexOutOfBoundsException s) {
            Log.d("INV", "Number of Container Errors:");
            s.printStackTrace();
            return "Error";
        } catch (NullPointerException npe){
            Log.d("INV", "Unknown NPE");
            npe.printStackTrace();
            return "Error";
        }
        return Table;
    }


    /**
     * Creates the serial Inventory Data Packet
     * @return The serial Inventory Data Packet.
     */
    public String SerialInventory(){
        String SerialCmd = "$INV,";
        SerialCmd += "9,9";
        Log.d("INV","SerInvStr:"+ SerialCmd);
        try {

            //Row Information
            for (int i = 0; i < NUM_CONTAINERS; i++) {
                SerialCmd += "@"+(i)+","+Containers[i].SerialContainer();
            }
            Log.d("INV","SerInvStr:"+SerialCmd);
            Log.d("INV","String is:"+SerialCmd.length()+" Long");
        } catch (StringIndexOutOfBoundsException s) {
            Log.d("INV", "Number of Container Errors:");
            s.printStackTrace();
            return "Error";
        } catch (NullPointerException npe){
            Log.d("INV", "Unknown NPE");
            npe.printStackTrace();
            return "Error";
        }
        return SerialCmd;
    }
//If you took the time to read this far,
// I'll give you a dollar next meeting.
}
