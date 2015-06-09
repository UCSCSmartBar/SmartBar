package com.example.trider.smartbarui;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by trider on 2/27/2015.
 */
public class DrinkStrings {

    /**Spirits**/
    private final static String AS = "Absinthe";
    private final static String BO = "Bourbon";
    private final static String BR = "Brandy";
    private final static String CG = "Cognac";
    private final static String EV = "EverClear";
    private final static String GB = "Ginger Beer";
    private final static String GN = "Gin";
    private final static String MO = "Moonshine";
    private final static String ME = "Mezcal";
    private final static String RM = "Rum";
    private final static String ST = "Scotch";
    private final static String TQ = "Tequila";
    private final static String VE = "Vermouth";
    private final static String VO = "Vodka";
    private final static String WH = "Whiskey";


    //Liqueurs
    private final static String AM = "Amaretto";       // (almond)
    private final static String BC = "Blue Curacao";   // (orange)
    private final static String BS = "Blood Orange IS"; // Blood orange italian soda
    private final static String MA = "Mango IS";//      // Mango
    private final static String CH = "Cherry IS";       // Cherry Italian Soda
    private final static String WM = "Watermelon IS";   //
    private final static String VA = "Vanilla IS";      //
    private final static String BE = "Benedictine";
    private final static String CD = "Chambord";       // (raspberry)
    private final static String CI = "Cointreau";      // (orange)
    private final static String CB = "Creme de Banana";// (banana)
    private final static String CC = "Creme de Cacao"; // (chocolate)
    private final static String CS = "Creme de Cassis";// (piquant berry)
    private final static String CM = "Creme de Menthe";// (mint)
    private final static String FR = "Frangelico";     // (hazelnut)
    private final static String GL = "Galliano";       // (herb)
    private final static String GO = "Godiva";         // (chocolate)
    private final static String GD = "Goldschlager";   // (cinnamon)
    private final static String GM = "Grand Marnier";  // (orange)
    private final static String JA = "Jagermeister";   // (herb)
    private final static String KA = "Kahlua";         // (coffee)
    private final static String KL = "Kina Lillet";
    private final static String LB = "Lillet Blanc";
    private final static String MI = "Midori";         // (melon)
    private final static String RU = "Rumple Minze";   // (peppermint)
    private final static String SA = "Sambuca";        // (anise)
    private final static String SN = "Schnapps";       // (various flavors)
    private final static String SC = "Southern Comfort";// (peach)
    private final static String TM = "Tia Maria";      // (coffee)
    private final static String TS = "Triple Sec";     // (orange)

    //Mixers
    private final static String AB = "Angostura Bitters";
    private final static String LE = "Lemonade";
    private final static String CO = "Cola";
    private final static String CL = "Club Soda";
    private final static String CR = "Cream";
    private final static String EG = "Eggs";
    private final static String GA = "Ginger Ale";
    private final static String GR = "Grenadine";
    private final static String GT = "Gatorade";
    private final static String IC = "Ice Cream";
    private final static String MK = "Milk";
    private final static String OB = "Orange Bitters";
    private final static String PJ = "Pineapple Juice";
    private final static String SS = "Simple Sugar";
    private final static String SM = "Sour Mix";
    private final static String SP = "Sprite/7-Up";
    private final static String CF = "Tea/Coffee";
    private final static String WA = "Water";
    private final static String SO = "Soda";
    private final static String TO = "Tonic";

    private final static String PF = "Passion Fruit IS"; // Blood orange italian soda


    public static Map<String, String> DrinkLibraryNames = new TreeMap<String,String>();



    //Liquid[] Library = new Liquid[100];

    public static void CreateLibrary(){
        DrinkLibraryNames.put("BS",BS);
        DrinkLibraryNames.put("MA",MA);

        DrinkLibraryNames.put("WM",WM);
        DrinkLibraryNames.put("VA",VA);

        DrinkLibraryNames.put("CD",CD);
        DrinkLibraryNames.put("AS",AS);
        DrinkLibraryNames.put("BO",BO);
        DrinkLibraryNames.put("BR",BR);
        DrinkLibraryNames.put("CG",CG);
        DrinkLibraryNames.put("CG",CG);
        DrinkLibraryNames.put("GT",GT);
        DrinkLibraryNames.put("EV",EV);
        DrinkLibraryNames.put("GB",GB);
        DrinkLibraryNames.put("GN",GN);
        DrinkLibraryNames.put("MO",MO);
        DrinkLibraryNames.put("ME",ME);
        DrinkLibraryNames.put("RM",RM);
        DrinkLibraryNames.put("ST",ST);
        DrinkLibraryNames.put("TQ",TQ);
        DrinkLibraryNames.put("VE",VE);
        DrinkLibraryNames.put("VO",VO);
        DrinkLibraryNames.put("WH",WH);

        DrinkLibraryNames.put("AM",AM);
        DrinkLibraryNames.put("BC",BC);
        DrinkLibraryNames.put("BE",BE);
        DrinkLibraryNames.put("CH",CH);
        DrinkLibraryNames.put("CI",CI);
        DrinkLibraryNames.put("CB",CB);
        DrinkLibraryNames.put("CC",CC);
        DrinkLibraryNames.put("CS",CS);
        DrinkLibraryNames.put("CM",CM);
        DrinkLibraryNames.put("FR",FR);
        DrinkLibraryNames.put("GL",GL);
        DrinkLibraryNames.put("GO",GO);
        DrinkLibraryNames.put("GD",GD);
        DrinkLibraryNames.put("GM",GM);
        DrinkLibraryNames.put("JA",JA);
        DrinkLibraryNames.put("KA",KA);
        DrinkLibraryNames.put("LB",LB);
        DrinkLibraryNames.put("PJ",PJ);
        DrinkLibraryNames.put("MI",MI);
        DrinkLibraryNames.put("RU",RU);
        DrinkLibraryNames.put("SA",SA);
        DrinkLibraryNames.put("SN",SN);
        DrinkLibraryNames.put("SC",SC);
        DrinkLibraryNames.put("SS",SS);
        DrinkLibraryNames.put("TM",TM);
        DrinkLibraryNames.put("TS",TS);

        DrinkLibraryNames.put("AB",AB);
        DrinkLibraryNames.put("CO",CO);
        DrinkLibraryNames.put("CF",CF);
        DrinkLibraryNames.put("CL",CL);
        DrinkLibraryNames.put("CR",CR);
        DrinkLibraryNames.put("EG",EG);
        DrinkLibraryNames.put("GA",GA);
        DrinkLibraryNames.put("GR",GR);
        DrinkLibraryNames.put("IC",IC);
        DrinkLibraryNames.put("KL",KL);
        DrinkLibraryNames.put("LE",LE);
        DrinkLibraryNames.put("MK",MK);
        DrinkLibraryNames.put("OB",OB);
        DrinkLibraryNames.put("SM",SM);
        DrinkLibraryNames.put("SP",SP);
        DrinkLibraryNames.put("SO",SO);
        DrinkLibraryNames.put("TO",TO);
        DrinkLibraryNames.put("WA",WA);

        DrinkLibraryNames.put("PF",PF);
    }


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

    /**
     * CodeToString decodes a drink code that shows up in recipes to its actual name. It searches through
     * a Hash Map of the library names.
     * e.g. "JA"-->"Jaegermaister"
     * @param CODE The two character identifier to be searched for e.g. "WH"
     * @return name The real string name for the searched name e.g. "Whiskey",
     *              "Unknown" for drink not in table.
     */
    public static String CodeToString(String CODE){
        Log.d("DS","InString:"+ CODE);
        for(Map.Entry<String,String> entry : DrinkLibraryNames.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            //Log.d("DS",key + " => " + value);
            if(key.equals(CODE)){return value;}

        }
        return null;
    }

    /**
     * KEY-->CO, Value--> Cola e.g.
     * @param string
     * @return
     */
    public static String StringToCode(String string){
        Log.d("DS","InString:"+ string);
        for(Map.Entry<String,String> entry : DrinkLibraryNames.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            //Log.d("DS",key + " => " + value);
            if(value.equals(string)){return key;}

        }
        return null;
    }
}
