package com.example.trider.smartbarui;

import android.util.Log;

/**
 * Created by trider on 2/23/2015.
 */
public class SystemCodeParser {



        public static String DecodeAccessoryMessage(String message) {
            if (message == null) {
                return null;
            }
            String aTokens[] = message.split("[@,+]");
            Log.d("SCP","Parsing Command " + aTokens[0]);
            switch (aTokens[0].trim()) {
                case ("$AD"):
                    DecodeADMessage(message);
                    return "$ACK|AD";
                case ("$SYS"):
                    DecodeSystemMessage(message);
                    return "$ACK|SYS";
                case ("$FP"):
                    DecodeScannerMessage(message);
                    return "$ACK|FP";
                case ("$BAC"):
                    DecodeBACMessage(message);
                    return "$ACK|BAC";
                case ("$VA"):
                    DecodePneumaticsMessage(message);
                    return "$ACK|VA";
                case ("$IV"):
                    DecodeInventoryMessage(message);
                    return "$ACK|LI";
                case ("$SER"):
                    return "$ACK|SER";
                case("$ACK"):
                    return "$ACK";
                case("$NACK"):
                    return "$NACK";
                //Unknown Error Code
                default:
                    return "NACK|UCMD";
            }
        }


        /*
        **@TODO Fill and decode the various error or warning messages
         */

        /**
         * Decodes any message dealing with the Analog To Digital Converter
         * @param message
         */
        private static void DecodeADMessage(String message){

        }
        /**
         * Decodes any message dealing with the Raspberry Pi Itself
         * @param message
         */
        private static void DecodeSystemMessage(String message){

        }
        /**
         * Decodes any message dealing with the Finger Print Scanner
         * @param message
         */
        private static void DecodeScannerMessage(String message){

        }
        /**
         * Decodes any message dealing with the BAC (Which may just be the AD)
         * @param message
         */
        private static void  DecodeBACMessage(String message){

        }
        /**
         * Decodes any message dealing with the Pneumatic System
         * @param message
         */
        private static void DecodePneumaticsMessage(String message){

        }
        /**
         * Decodes any message dealing with the Liquid Levels of the System
         * @param message
         */
        private static void DecodeInventoryMessage(String message){

        Inventory INV = new Inventory();
        Log.d("DIM", "In Message:" + message);
         //Full message e.g. $IV,2,2@0,WH,1,54.3,59.2@...
        String[] iTokens = message.split("[@+]");
            //Split message: [IV,2,2][0,WH,1,54.3,59.2]

            //[IV][2][2]
            Log.d("DIM","Inventory == [IV,i,i]?"+iTokens[0]+"\n");


            for(int i = 1; i < iTokens.length; i++){

                Log.d("DIM","Parsed Message"+iTokens[i]+"\n");

                String[] infoTokens = iTokens[i].split("[,+]");
                  // [Con#],[Type],[Brand],[curVol],[maxVol]


                    Log.d("DIM","   Number of Comp: " + infoTokens.length);
                    if(infoTokens.length < 5){
                        Log.d("DIM","Not enough info");
                        return;
                    }
                    Log.d("DIM","Container #: " + infoTokens[0]);
                    Log.d("DIM","Type: " +  infoTokens[1]);
                    Log.d("DIM","Brand: " + infoTokens[2]);
                    Log.d("DIM","CurVol " + infoTokens[3]);
                    Log.d("DIM","MaxVol " + infoTokens[4]);

                try {
                    int conNum = Integer.parseInt(infoTokens[0].trim()) + 1;
                    String type  = infoTokens[1];
                    String brand = infoTokens[2];
                    float CurVol= Float.parseFloat(infoTokens[3].trim());
                    float MaxVol= Float.parseFloat(infoTokens[4].trim());

                    INV.AddToInventory(conNum,type,brand,CurVol,MaxVol);

                    Log.d("DIM","New Container: "+INV.getContainer(conNum).PrintContainer());

                }catch(NumberFormatException nfe){
                    nfe.printStackTrace();
                }catch(NullPointerException npe){
                    npe.printStackTrace();
                }
                //Log.d("SCP","\n");
            }



           //INV.AddToInventory();

        }




}
