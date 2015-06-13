package com.example.trider.smartbarui;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by trider on 2/23/2015.
 */
public class SystemCodeParser {

    static Boolean CupReady = false;

    public static String DecodeAccessoryMessage(String message) {
        if (message == null) {
            return null;
        }
        try {
            Boolean ret;
            String aTokens[] = message.split("[@,+]");
            Log.d("SCP", "Parsing Command " + aTokens[0]);
            switch (aTokens[0].trim()) {
                case ("$AD"):
                    DecodeADMessage(message);
                    return "$ACK|AD";
                case ("$SYS"):
                    DecodeSystemMessage(message);
                    return "$ACK|SYS";
//                    case ("$FP"):
//                        DecodeScannerMessage(message);
//                        return "$ACK|FP";
//                    case ("$BAC"):
//                        DecodeBACMessage(message);
//                        return "$ACK|BAC";
                case ("$VA"):
                    DecodePneumaticsMessage(message);
                    return "$ACK|VA";
                case ("$IV"):
                    DecodeInventoryMessage(message);
                    return "$ACK|LI";
                case ("$CUP"):
                    ret = DecodeCupMessage(message);

                    if (ret) {
                        return "$ACK|CUP";
                    } else {
                        //return "$NACK|CUP"
                        return "$NACK|CUP";
                    }
                case ("$SER"):
                    return "$ACK|SER";
                case ("$WATCH"):
                    DecodeCommMessage(message);
                    return "$ACK|WATCH";
                case ("$ACK"):
                    return null;
                case ("$NACK"):
                    return null;
                //Unknown Error Code
                default:
                    return null;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        return null;
    }

        /*
        **@TODO Fill and decode the various error or warning messages
         */

    /**
     * Decodes any message dealing with the Analog To Digital Converter
     *
     * @param message
     */
    private static void DecodeADMessage(String message) {

    }

    /**
     * Decodes any message dealing with the Analog To Digital Converter
     *
     * @param message
     */
    private static Boolean DecodeCupMessage(String message) {
        try {
            String aTokens[] = message.split("[@,+]");
            if (aTokens[1] == null) {
                return false;
            }
            if (aTokens[1].equals("READY")) {
                CupReady = true;
                return true;
            } else {
                return false;
            }
        } catch (NullPointerException npe) {

        }
        return false;
    }

    /**
     * Decodes any message dealing with the Raspberry Pi Itself
     *
     * @param message
     */
    private static void DecodeSystemMessage(String message) {

    }

    /**
     * Decodes any message dealing with the Finger Print Scanner
     *
     * @param message
     */
    private static void DecodeScannerMessage(String message) {

    }

    /**
     * Decodes any message dealing with the BAC (Which may just be the AD)
     *
     * @param message
     */
    private static void DecodeBACMessage(String message) {

    }

    /**
     * Decodes any message dealing with the Pneumatic System
     *
     * @param message
     */
    private static void DecodePneumaticsMessage(String message) {

    }

    /**
     * Decodes any message dealing with the Liquid Levels of the System
     *
     * @param message
     */
    private static void DecodeInventoryMessage(String message) {
        //Number of mixers/ liquors
        int NOM = 0;
        int NOL = 0;
        //Creates new inventory
        Inventory INV = new Inventory();
        Log.d("DIM", "In Message:" + message);
        //Full message e.g. $IV,2,2@0,WH,1,54.3,59.2@...
        String[] iTokens = message.split("[@+]");
        //Split message: [IV,2,2][0,WH,1,54.3,59.2]

        //[IV][2][2]
        Log.d("DIM", "Inventory == [IV,i,i]?" + iTokens[0] + "\n");

        //Checks the header of the inventory string
        String Header[] = iTokens[0].split("[,]");
        Log.d("DIM", "Header is:");
        for (String s : Header) {
            if (s != null) {
                Log.d("DIM", s);
            }
        }
        //Parses out the number of liquors/mixers
        try {
            NOL = Integer.valueOf(Header[1]);
            NOM = Integer.valueOf(Header[2]);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException aoie){
            Log.d("DIM", "Invalid Header"+iTokens[0]);
        }
        //Parses through each token of the ivnentory string to extract information
        for (int i = 1; i < iTokens.length; i++) {

            iTokens[i] = iTokens[i].replace("$IV,", ""); //Replaces weird $IV from Pi
            Log.d("DIM", "Parsed Message " + iTokens[i] + "\n");
            String[] infoTokens = iTokens[i].split("[,+]");
            // [Con#],[Type],[Brand],[curVol],[maxVol]


            Log.d("DIM", "   Number of Components: " + infoTokens.length);
            if (infoTokens.length < 5) {
                Log.d("DIM", "Not enough info");
                return;
            }
            Log.d("DIM", "Container #: " + infoTokens[0]);
            Log.d("DIM", "Type: " + infoTokens[1]);
            Log.d("DIM", "Brand: " + infoTokens[2]);
            //Log.d("DIM","CARB: "+   infoTokens[3]);//Might not be doing carb
            Log.d("DIM", "CurVol " + infoTokens[3]);
            Log.d("DIM", "MaxVol " + infoTokens[4]);
            //String to Number conversions require catch statement
            try {
                int conNum = Integer.parseInt(infoTokens[0].trim()) + 1; //TODO make sure conNum is 1-18 and not 0-17
                String type = infoTokens[1];
                String brand = infoTokens[2];
                //int carb = Integer.parseInt(infoTokens[3]);
                float CurVol = Float.parseFloat(infoTokens[3].trim());
                float MaxVol = Float.parseFloat(infoTokens[4].trim());

                //Updates selected container's volume
                INV.getContainer(conNum).setCurVolume(CurVol);
                //Doesnt set price/carbed
                int carb;
                if (i-1 < NOL) {
                    Log.d("DIM", infoTokens[1] + " is not carbonated");
                    carb = 0;
                } else {
                    Log.d("DIM", infoTokens[1] + "is carbontaed");
                    carb = 1;
                }

                INV.AddToInventory(conNum, type, brand, carb, CurVol, MaxVol, 0);
                //INV.AddToInventory(conNum,type,brand,carb,CurVol,MaxVol,0.0);

                Log.d("DIM", "New Container: " + INV.getContainer(conNum).PrintContainer());

            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            //Log.d("SCP","\n");
        }


    }

    private static void DecodeCommMessage(String message) {
        if (message.contains("OKAY") || message.contains("MEOW")) {
            CommStream.PetBeagle();
        }
    }


}
