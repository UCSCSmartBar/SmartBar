package com.example.trider.smartbarui;

/**
 * Created by trider on 2/23/2015.
 */
public class SystemCodeParser {

        public void DecodeAccessoryMessage(String message) {
            if (message == null) {
                return;
            }
            String aTokens[] = message.split("[@+]");
            switch (aTokens[0].trim()) {
                case ("$AD"):
                    DecodeADMessage(message);
                    break;
                case ("$SYS"):
                    DecodeSystemMessage(message);
                    break;
                case ("$FP"):
                    DecodeScannerMessage(message);
                    break;
                case ("$BAC"):
                    DecodeBACMessage(message);
                    break;
                case ("$VA"):
                    DecodePneumaticsMessage(message);
                    break;
                case ("$LI"):
                    DecodeLiquidsMessage(message);
                    break;
                case ("$SER"):
                    break;

                //Unknown Error Code
                default:
                    break;
            }
        }


            /*
            **@TODO Fill and decode the various error or warning messages
             */
            public void DecodeADMessage(String message){

            }
            public void DecodeSystemMessage(String message){

            }
            public void DecodeScannerMessage(String message){

            }
            public void DecodeBACMessage(String message){

            }
            public void DecodePneumaticsMessage(String message){

            }
            public void DecodeLiquidsMessage(String message){

            }

}
