package com.example.trider.smartbarui;

import android.util.Log;

/**
 * Created by trider on 2/13/2015.
 * The Drink Order class is used for both packing custom drinks(not currently in use), and decoding
 * sent drinks. A DrinkOrder Object can be split into sub-elements called Liquor and Mixer Objects,
 * each with data about the spirit,brand and volume of each individual element. Currently the class
 * is being used as a means to parse the drink orders from the queue, and double check if they are
 * of a valid amount or if the string is a valid string.
 */
public class DrinkOrder {


    public static String InDrinkString = null;
    public String OutDrinkString;
    public static String InUserPinString = null;

    private static final String StartOfDString = "$DO";
    private static final Byte EndOfDString = '*';
    private LiquorObj[] liquors;
    private MixerObj[]  mixers;
    private int MAX_INGREDIENTS = 20;
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
        liquors = new LiquorObj[MAX_INGREDIENTS ];
        mixers = new MixerObj[MAX_INGREDIENTS ];
        lIndex = 0;
        mIndex = 0;
    }
    public DrinkOrder(int nMixers,int nLiquors){
        if((nMixers > MAX_INGREDIENTS) || (nMixers > MAX_INGREDIENTS) ||(nLiquors < 0) || (nMixers < 0)) {
            Log.d("DO","Invalid dimensions");
    }

        liquors = new LiquorObj[nLiquors];
        mixers = new MixerObj[nMixers];
        lIndex = 0;
        mIndex = 0;
    }

    //Adds one liquor Object to the order
    public void AddLiqDrinkOrder(LiquorObj liquor){
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

    /**
     * Adds a liquor object to the order
     * @param type Type of alcohol being added
     * @param brand Brand of Liquor
     * @param oz The volume
     */
    public void AddLiqDrinkOrder(int type, int brand, int oz){
        if((lIndex + mIndex) >= 18){
            Log.d("DO","Tried adding too much to order");
            return;
        }

        liquors[lIndex] = new LiquorObj(type,brand,oz);
        lIndex++;
        numLiquors++;
        Log.d("DO","Succesfully Added \n"+liquors[lIndex-1].toString()+ "\n to Drink Order:" + liquors[lIndex-1]);
    }

    //Adds one mixer Object to the order
    public void AddMixDrinkOrder(MixerObj mixer){
        if((lIndex + mIndex) >= 18){
            return;
        }else if(mixer == null){
            return;
        }
        mixers[mIndex] = mixer;
        mIndex++;
        numMixers++;
        Log.d("DO","Succesfully Added \n"+mixer+ "\n to Drink Order:" + liquors[lIndex-1]);
    }

    /**
     * Adds a liquor object to the order
     * @param type Type of alcohol being added
     * @param brand Brand of Liquor
     * @param oz The volume
     */
    public void AddMixDrinkOrder(int type, int brand, Boolean carbonated, int oz){
        if((lIndex + mIndex) >= 18){
            return;
        }

        mixers[mIndex] = new MixerObj(type,brand,carbonated,oz);
        mIndex++;
        numMixers++;
        Log.d("DO","Succesfully Added \n"+mixers[lIndex-1].toString()+ "\n to Drink Order:" + liquors[lIndex-1]);
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

    public void storeDrinkOrder(String s){
        InDrinkString = s;
    }

    public String getCurrentDrinkOrder(){
        return InDrinkString;
    }
//@TODO decode string before forwarding it to Raspberry Pi to check if feasible drink.

    /**
     * Parses Up an incoming string into a drink order, and a table of information about drink
     * @param s
     * @return A table about the drink order
     */
    public String DecodeString(String s){
        if(s == null){return null;}

        Log.d("DParse","Incoming String:" +s);
        int NOL;
        int NOM;
        float vol = 0;
        String outGoingTable;
        String[] tokens;

        //Takes a String of 1,2@V,0,.1@ ...... and converts it to [0,0,1] [1,2,3]
        tokens = s.split("[@*+]");


        outGoingTable = "Spirits:\n";
        //Number of Mixers/Liquors Identifiers
        Log.d("DParse","Token[0]:"+tokens[0] + "\n");
        String[] aTokens =  tokens[0].split("[,*+]");

        try{
            NOL = Integer.valueOf(aTokens[0].trim());
            NOM = Integer.valueOf(aTokens[1].trim());
            Log.d("DParse","NOL["+NOL+"] NOM["+NOM+"]\n");
        }catch(NumberFormatException i){
            i.printStackTrace();
            return "Error";
        }

        DrinkOrder newOrder = new DrinkOrder(NOL,NOM);

        outGoingTable = "Spirit:\n";
        int i = 1;
        //Todo Convert the strings incoming strings to the approriate drink order code.
        for(; i < tokens.length - NOM; i++){
           Log.d("Dparse","Tokens["+i+"] Liquor{"+tokens[i] + "}\n");
           String[] lTokens = tokens[i].split("[,+]");
               Log.d("Dparse","Spirit:" + lTokens[0]);
               Log.d("Dparse","Brand:" + lTokens[1]);
               Log.d("Dparse","Volume" + lTokens[2]);
           outGoingTable+=lTokens[0] + ":" + lTokens[1]+":" +lTokens[2]+"\n";

           //Adds to total volume of drink
           try {
               vol += Float.parseFloat(lTokens[2].trim());
           }catch(NumberFormatException e){
               e.printStackTrace();
           }

           }


         outGoingTable +="Mixers:\n";
        //Loop through Mixers and print out individual components
        for(;i < tokens.length;i++) {
           Log.d("Dparse", "Tokens[" + i + "] Mixer{" + tokens[i] + "}\n");

           String[] mTokens = tokens[i].split("[,+]");
               Log.d("Dparse","Mixer:" + mTokens[0]);
               Log.d("Dparse","Brand:" + mTokens[1]);
               Log.d("Dparse","Carb:" +  mTokens[2]);
               Log.d("Dparse","Volume" + mTokens[3]);
           outGoingTable+=mTokens[0] + ":" + mTokens[1]+":"+ mTokens[3]+"\n";

           try {
               vol += Float.parseFloat(mTokens[3]);
           }catch(NumberFormatException e){
               e.printStackTrace();
           }
        }

        Log.d("Dparse","Total Volume["+vol+"]");

        return outGoingTable;
    }





}
