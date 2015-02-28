package com.example.trider.smartbarui;

/**
 * Created by trider on 2/23/2015.
 */
public class SystemCodeParser {



        public String DecodeAccessoryMessage(String message) {
            if (message == null) {
                return null;
            }
            String aTokens[] = message.split("[@,+]");
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
                case ("$LI"):
                    DecodeLiquidsMessage(message);
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
        private void DecodeADMessage(String message){

        }
        /**
         * Decodes any message dealing with the Raspberry Pi Itself
         * @param message
         */
        private void DecodeSystemMessage(String message){

        }
        /**
         * Decodes any message dealing with the Finger Print Scanner
         * @param message
         */
        private void DecodeScannerMessage(String message){

        }
        /**
         * Decodes any message dealing with the BAC (Which may just be the AD)
         * @param message
         */
        private void DecodeBACMessage(String message){

        }
        /**
         * Decodes any message dealing with the Pneumatic System
         * @param message
         */
        private void DecodePneumaticsMessage(String message){

        }
        /**
         * Decodes any message dealing with the Liquid Levels of the System
         * @param message
         */
        private void DecodeLiquidsMessage(String message){

        }

}
