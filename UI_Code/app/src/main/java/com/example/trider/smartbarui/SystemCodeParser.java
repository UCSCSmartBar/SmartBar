package com.example.trider.smartbarui;

/**
 * Created by trider on 2/23/2015.
 */
public class SystemCodeParser {

<<<<<<< HEAD
=======


>>>>>>> 313167a7340a7180bd643478785395b38af4d4d3
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
<<<<<<< HEAD
        public void DecodeADMessage(String message){
=======
        private void DecodeADMessage(String message){
>>>>>>> 313167a7340a7180bd643478785395b38af4d4d3

        }
        /**
         * Decodes any message dealing with the Raspberry Pi Itself
         * @param message
         */
<<<<<<< HEAD
        public void DecodeSystemMessage(String message){
=======
        private void DecodeSystemMessage(String message){
>>>>>>> 313167a7340a7180bd643478785395b38af4d4d3

        }
        /**
         * Decodes any message dealing with the Finger Print Scanner
         * @param message
         */
<<<<<<< HEAD
        public void DecodeScannerMessage(String message){
=======
        private void DecodeScannerMessage(String message){
>>>>>>> 313167a7340a7180bd643478785395b38af4d4d3

        }
        /**
         * Decodes any message dealing with the BAC (Which may just be the AD)
         * @param message
         */
<<<<<<< HEAD
        public void DecodeBACMessage(String message){
=======
        private void DecodeBACMessage(String message){
>>>>>>> 313167a7340a7180bd643478785395b38af4d4d3

        }
        /**
         * Decodes any message dealing with the Pneumatic System
         * @param message
         */
<<<<<<< HEAD
        public void DecodePneumaticsMessage(String message){
=======
        private void DecodePneumaticsMessage(String message){
>>>>>>> 313167a7340a7180bd643478785395b38af4d4d3

        }
        /**
         * Decodes any message dealing with the Liquid Levels of the System
         * @param message
         */
<<<<<<< HEAD
        public void DecodeLiquidsMessage(String message){
=======
        private void DecodeLiquidsMessage(String message){
>>>>>>> 313167a7340a7180bd643478785395b38af4d4d3

        }

}
