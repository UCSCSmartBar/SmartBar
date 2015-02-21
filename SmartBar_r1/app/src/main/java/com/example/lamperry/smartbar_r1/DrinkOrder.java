package com.example.trider.smartbarui;

import android.util.Log;

/**
 * Created by trider on 2/13/2015.
 */
public class DrinkOrder {


    public String DrinkString;
    private static final String StartOfDString = "$DO";
    private static final Byte EndOfDString = '*';
    private LiquorObj[] liquors;
    private MixerObj[]  mixers;
    private int MAX_DRINKS = 20;
    private int numLiquors = 0;
    private int numMixers = 0;
    private int lIndex;
    private int mIndex;

    /**
     * The Liquor Object is part of the drink order, that when added to the order adds on a string
     * of @[Type][Brand][oz]. The liquor objects can be created outside this class, but are only
     * useful to adding to a drink order.
     */

    public static class LiquorObj {
        private static final byte StartOfLString = '@';
        public byte Type;
        public byte Brand;
        public byte Oz;
        public byte[] LiquorBytes;
        //Constructor of one element of the that contains
        //one specific type of liquor of a specific quantity
        LiquorObj(int type, int brand, int oz) {
            if (type < 256 && brand < 256 && oz < 256) {
                Type = (byte) type;
                Brand = (byte) brand;
                Oz = (byte) oz;
                LiquorBytes = new byte[]{StartOfLString,Type,Brand,Oz};
            } else {
                Log.d("Liq", "Failed creating liquor object");
                return;
            }
        }
        //Data dump on liquor object
        public String toString(){
            return "Type :" + Byte.toString(Type)   + "\n"+
                    "Brand:" + Byte.toString(Brand)  + "\n"+
                    "Oz:" + Byte.toString(Oz)+ "\n";
        }

        /**serString
         * Converts LiquorObj into a serializable form, to be concatinated with the rest of
         * the drink order
         * @return a string in the form "@[Type][Brand][Oz]"
         */
        public String serString(){
            String ser = Byte.toString(StartOfLString) +  Byte.toString(Type) +
                    Byte.toString(Brand)+  Byte.toString(Oz);
            Log.d("Ser","Mixer:" + ser);
            return ser;
        }

    }
    /**
     * The Mixer Object is part of the drink order, that when added to the order adds on a string
     * of @[Type][Brand][oz][carbonated]. The mixer objects can be created outside this class,
     * but are only useful to adding to a drink order.
     */
    public static class MixerObj {
        private static final byte StartOfMString = '&';
        public byte Type;
        public byte Brand;
        public byte Oz;
        public byte carbonated;
        public byte[] MixerBytes;
                /*Constructor*/

        MixerObj(int type, int brand, boolean carb, int oz) {
            if (type < 256 && brand < 256 && oz < 256) {
                Type = (byte) type;
                Brand = (byte) brand;
                Oz = (byte) oz;
                if(carb){
                    carbonated = (byte) 1;}
                else{
                    carbonated = 0;
                }
                MixerBytes = new byte[]{StartOfMString,Type,Brand,carbonated,Oz};
            } else {
                Log.d("Mix", "Failed creating mixer object");
                return;
            }
        }

        public String toString(){
            return "Type :" + Byte.toString(Type)     +"\n"+
                    "Brand:" + Byte.toString(Brand)   +"\n"+
                    "Oz:" + Byte.toString(Oz) +"\n"+
                    "Cab:" + Byte.toString(carbonated)+"\n";
        }
        /**serString
         * Converts MixerObj into a serializable form, to be concatinated with the rest of
         * the drink order
         * @return a string in the form "@[Type][Brand][Oz][carb]"
         */
        public String serString(){

            String ser = Byte.toString(StartOfMString) +  Byte.toString(Type) +
                    Byte.toString(Brand)+  Byte.toString(Oz)+ Byte.toString(carbonated);
            Log.d("Ser","Mixer:" + Byte.toString(MixerBytes[1])+ ser);
            return ser;
        }
    }

    //Declares new DrinkOrder that can have multiple items added to it
    public DrinkOrder(){
        liquors = new LiquorObj[10];
        mixers = new MixerObj[10];
        lIndex = 0;
        mIndex = 0;
    }

    //Adds one liquor Object to the order
    public void AddToDrinkOrder(LiquorObj liquor){
        if((lIndex + mIndex) >= 18){
            return;
        }else if(liquor == null){
            Log.d("DO","Tried adding Null Drink");
            return;
        }

        liquors[lIndex] = liquor;
        lIndex++;
        numLiquors++;
        Log.d("DO","Succesfully Added \n"+liquor+ "\n to Drink Order:" + liquors[lIndex-1]);
    }

    //Adds one mixer Object to the order
    public void AddToDrinkOrder(MixerObj mixer){
        if((lIndex + mIndex) >= 18){
            return;
        }else if(mixer == null){
            return;
        }
        mixers[mIndex] = mixer;
        mIndex++;
        numMixers++;
    }

    //Gives a data dump about the onGoing Drink Order
    public String toString(){
        String DrinkData;
        DrinkData = "Liquors: \n";
        for(int i = 0; i < numLiquors ; i++){
            DrinkData = DrinkData + liquors[i].toString();
        }
        DrinkData = DrinkData + "Mixers: \n";
        for(int i = 0; i < numMixers ; i++){
            DrinkData = DrinkData + mixers[i].toString();
        }
        return DrinkData;
    }

    /**
     * The Serial Drink method composes the serial data byte by byte into a sendable form over the
     * communication link
     * @return a string in the form "$DO[LiquorObj][LiquorObj]...[MixerObj][MixerObj]*"
     */
    public String serialDrink(){
        String serialMessage = StartOfDString;
        Log.d("ser", "\nSerial Message: "+ serialMessage);
        for(int i = 0; i < numLiquors; i++){
            serialMessage = serialMessage + liquors[i].serString();
        }
        Log.d("ser", "\nSerial Message: "+ serialMessage+ "\n");
        for(int i = 0; i < numMixers; i++){
            serialMessage = serialMessage + mixers[i].serString();
        }
        Log.d("ser", "\nSerial Message: "+ serialMessage+ "\n");

        serialMessage = serialMessage + "*";
        Log.d("ser", "\nSerial Message: "+ serialMessage);
        return serialMessage;
    }

}
