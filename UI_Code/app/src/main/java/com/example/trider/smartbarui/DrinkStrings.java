package com.example.trider.smartbarui;

import android.util.Log;

/**
 * Created by trider on 2/27/2015.
 */
public class DrinkStrings {

    /**Spirits**/
    private final String AS = "Absinthe";
    private final String BO = "Bourbon";
    private final String BR = "Brandy";
    private final String CG = "Cognac";
    private final String EV = "EverClear";
    private final String GN = "Gin";
    private final String MO = "Moonshine";
    private final String ME = "Mezcal";
    private final String RM = "Rum";
    private final String ST = "Scotch";
    private final String TQ = "Tequila";
    private final String VE = "Vermouth";
    private final String VO = "Vodka";
    private final String WH = "Whiskey";


    //Liqueurs
    private final String AM = "Amaretto";       // (almond)
    private final String BC = "Blue Curacao";   // (orange)
    private final String CH = "Chambord";       // (raspberry)
    private final String CI = "Cointreau";      // (orange)
    private final String CB = "Creme de Banana";// (banana)
    private final String CC = "Creme de Cacao"; // (chocolate)
    private final String CM = "Creme de Menthe";// (mint)
    private final String FR = "Frangelico";     // (hazelnut)
    private final String GL = "Galliano";       // (herb)
    private final String GO = "Godiva";         // (chocolate)
    private final String GD = "Goldschlager";   // (cinnamon)
    private final String GM = "Grand Marnier";  // (orange)
    private final String JA = "Jagermeister";   // (herb)
    private final String KA = "Kahlua";         // (coffee)
    private final String MI = "Midori";         // (melon)
    private final String RU = "Rumple Minze";   // (peppermint)
    private final String SA = "Sambuca";        // (anise)
    private final String SN = "Schnapps";       // (various flavors)
    private final String SC = "Southern Comfort";// (peach)
    private final String TM = "Tia Maria";      // (coffee)
    private final String TS = "Triple Sec";     // (orange)

    //Mixers
    private final String AB = "Angostura Bitters";
    private final String LE = "Lemonade";
    private final String CO = "Cola";
    private final String CR = "Cream";
    private final String EG = "Eggs";
    private final String GA = "Ginger Ale";
    private final String GR = "Grenadine";
    private final String IC = "Ice Cream";
    private final String MK = "Milk";
    private final String OB = "Orange Bitters";
    private final String SM = "Sour Mix";
    private final String SP = "Sprite/7-Up";
    private final String CF = "Tea/Coffee";
    private final String WA = "Water";
    private final String SO = "Soda";
    private final String TO = "Tonic";

    public class Liquid{
        String Spirit;
        String Name;
    }

    Liquid[] Library = new Liquid[100];

    /**
     * Converts a String Brand name such as Jameson Irish Whiskey to JI, by taking first letter of each
     * name
     * @param s The full string name e.g. Jack Daniel's
     * @return An identifier such as JD
     */
    public static String BrandToCode(String s) {

        if (s == null) {
            return "Error";
        }
        String sTokens[] = s.split("[\\s+]");

        for (int i = 0; i < sTokens.length; i++) {
            Log.d("DrinkS", "sToken[" + i + "]:" + sTokens[i]);
        }

        try {
            int a = Integer.parseInt(s.trim());
            //Don't do anything if it is a number
            return s;

        }catch(NumberFormatException nfe){
            //nfe.printStackTrace();
        }
        //If One name identifier
        if(sTokens.length==1){
            Log.d("DrinkS","["+s+"]["+s.substring(0,2).toUpperCase() );
            return s.substring(0,1).toUpperCase();
        }else{
            String a = sTokens[0].substring(0,1).toUpperCase() + sTokens[1].substring(0,1).toUpperCase();
            Log.d("DrinkS","["+s+"]["+a+"]" );
            return a;
        }

    }

    /**
     * Converts a String Brand name such as Jameson Irish Whiskey to JI, by taking first letter of each
     * name
     * @param s The full string name e.g. Jack Daniel's
     * @return An identifier such as JD
     */
    public static String SpiritToCode(String s){

        if (s == null) {
            return "Error";
        }
        String sTokens[] = s.split("[\\s+]");

        for (int i = 0; i < sTokens.length; i++) {
            Log.d("DrinkS", "sToken[" + i + "]:" + sTokens[i]);
        }

        try {
            int a = Integer.parseInt(s.trim());
            //Don't do anything if it is a number
            return s;

        }catch(NumberFormatException nfe){
            //nfe.printStackTrace();
        }

        //If One name identifier
        if(sTokens.length==1){
            Log.d("DrinkS","["+s+"]["+s.substring(0,2).toUpperCase() );
            return s.substring(0,2).toUpperCase();
        }else{
            String a = sTokens[0].substring(0,1).toUpperCase() + sTokens[1].substring(0,1).toUpperCase();
            Log.d("DrinkS","["+s+"]["+a+"]" );
            return a;
        }


    }


    public static String CodeToString(String s){



        return null;
    }
}
