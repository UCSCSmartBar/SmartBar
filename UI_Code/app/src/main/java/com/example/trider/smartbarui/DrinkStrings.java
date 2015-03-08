package com.example.trider.smartbarui;

import android.util.Log;

/**
 * Created by trider on 2/27/2015.
 */
public class DrinkStrings {

    private final String V = "Vodka";
    private final String W = "Whiskey";
    private final String G = "Gin";
    private final String B = "Bourbon";
    private final String T = "Tequila";
    private final String L = "LemonJuice";


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
    public static String TypeToCode(String s){

        if(s==null){return"Error";}
        String sTokens[] = s.split("[ +]");
        //If One name identifier
        if(sTokens.length==1){
            Log.d("DrinkS","["+s+"]["+s.substring(0,1).toUpperCase() );
            return s.substring(0,1).toUpperCase();
        }else{
            String a = sTokens[0].substring(0,0).toUpperCase() + sTokens[1].substring(0,0).toUpperCase();
            Log.d("DrinkS","["+s+"]["+a );
            return a;
        }

    }
}
