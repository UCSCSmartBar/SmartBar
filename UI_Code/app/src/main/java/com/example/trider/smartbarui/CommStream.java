package com.example.trider.smartbarui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by trider on 2/6/2015.
 */


public class CommStream {
    //Communication system variables
    private static FileInputStream mInputStream;
    private static FileOutputStream mOutputStream;

    private static boolean initialized = false;
    private static UsbAccessory usbAcc;
    private static UsbManager usbMan;
    private static ParcelFileDescriptor parcelFD;



    public static final String Status_Created          =    "UNINITIALIZED_COMM_CREATED";
    public static final String Status_Connected        =    "USB_CONNECTED";
    public static final String Status_Disconnected     =    "USB_DISCONNECTED";
    public static final String Status_Waiting          =    "USB_WAITING";


    public static final Boolean ON = true;
    public static final Boolean OFF = false;

    public static String StatusString = "test0";

    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_ACCESSORY_PACKET = 16384;
    private static String[] readBuffer = new String[BUFFER_SIZE];
    private static int readIndex = 0;
    private static int writeIndex = 0;
    private static String in;

    public int ret;
    //Status Flags
    public static boolean SYSTEM_FINGERPRINT = ON;
    public static boolean SYSTEM_BAC         = ON;
    public static boolean SYSTEM_SERVER      = ON;
    public static boolean SYSTEM_PI          = ON;
    public static boolean SYSTEM_STATUS      = OFF;
    public static boolean DEBUG_MODE         = OFF;
    public static boolean TEST_HARNESS       = OFF;
    public static boolean FATAL_ERROR        = OFF;

    public static boolean _INTERNETCONN      = OFF;

    //Warning Flags
    public static byte DS_ER_WA_FLAGS = 0x00;
    public static byte DS_ERROR = 0x01;
    public static byte DS_NAK   = 0x02;

    public static void setFlag(byte flag){
        DS_ER_WA_FLAGS |= flag;
    }

    public static void ClearFlags(){
        DS_ER_WA_FLAGS = 0x00;
    }







    //Error Codes
    public enum ErrorCode{
        _ERROR_NONE,
        _ERROR_USB_UNPLUGGED,
        _ERROR_COMM_TIMED_OUT,
    }




    public ErrorCode Comm_Error_Status = ErrorCode._ERROR_NONE;

    public enum WatchState{
         CONNECTED ,
            WAITING_FOR_RESPONSE,
        WARNING,
         DISCONNECTED ,
    }
    /**
     * The CommWatchDog is a class that ensures the two devices are connected by both periodically
     */

    public class CommWatchDog {
        final static int _LONG_TIME     = 20;//5 min
        final static int _SHORT_TIME    = 15;//2 min
        final static int _AWHILE        = 10;//1 min
        Boolean initialized;

        int TIME_TILL_WALK;
        WatchState STATUS;
        //The actual timer
         Timer Lookout;
         TimerTask UpDate = new TimerTask() {
            @Override
            public void run() {
                TIME_TILL_WALK++;
                if(STATUS == WatchState.CONNECTED){
                        if(TIME_TILL_WALK>_AWHILE) {
                            writeString("$WATCH,WOOF");
                            STATUS = WatchState.WAITING_FOR_RESPONSE;
                            CommStream.StatusString = Status_Waiting;
                        }
                }else if(STATUS == WatchState.WAITING_FOR_RESPONSE) {
                    if (TIME_TILL_WALK > _SHORT_TIME) {
                        STATUS = WatchState.WARNING;
                    }

                }else if(STATUS == WatchState.WARNING){
                    writeString("$WATCH,BARK");
                    if(TIME_TILL_WALK > _LONG_TIME){
                        STATUS = WatchState.DISCONNECTED;
                        CommStream.StatusString = Status_Disconnected;

                    }
                }else if(STATUS == WatchState.DISCONNECTED){

                }
            }
        };

        /**
         * Initializes the Watch Dog Timer
         */
        CommWatchDog(){
            TIME_TILL_WALK = 0;
            writeString("Starting Watch");
            STATUS = WatchState.CONNECTED;
            Lookout = new Timer();
            Lookout.scheduleAtFixedRate(UpDate,1000,1000);//Occurs every one second
            initialized = true;
        }

        @Override
        public String toString() {
            return String.format("Current Status:"+STATUS.name()+"\nCurrent Time:"+TIME_TILL_WALK+"\n");
        }

        public Boolean isInitialized(){return initialized;}


        public void PetWatchDog(){
            TIME_TILL_WALK = 0;
            STATUS = WatchState.CONNECTED;
            CommStream.StatusString = CommStream.Status_Connected;
        }

    }


    public static CommWatchDog Beagle;


    public static void PetBeagle(){
        if(Beagle.isInitialized()) {
            Beagle.PetWatchDog();
        }
    }
    /**
     * Creates New communication Link between Device and Accessory, while storing data amount memory
     * locations of communication streams
     * @param iStream The InputStream to read from.
     * @param oStream The OutputStream to write to
     * @param uAcc    The accessory object.
     * @param uMan    The USB manager
     * @param PFD     The parcel File descriptor used to contain usb accessory/manager.
     */
    public CommStream(FileInputStream iStream, FileOutputStream oStream,UsbAccessory uAcc,
                                                  UsbManager uMan,ParcelFileDescriptor PFD){
        if(iStream != null && oStream != null) {
            mInputStream = iStream;
            mOutputStream = oStream;
            usbAcc = uAcc;
            usbMan = uMan;
            parcelFD = PFD;
            initialized = true;
            StatusString = Status_Connected;
//            Beagle = new CommWatchDog();
        }
    }

    public void StartBeagle(){
        Beagle = new CommWatchDog();
    }
    /**
     * Returns a reference to this class and static members.
     */
    public CommStream(){
        if(initialized){return;}
        StatusString = Status_Created;
    }

    /**
     * Creates a CommStream with a custom status flag. Useful to confirm static membership
     * @param s A custom string the status can be
     */
    public CommStream(String s){
        StatusString = s;
    }

    /**
     * Checks if initialized CommStream
     * @return True if there is a connection with the Pi. False otherwise.
     */
    public boolean isInitialized(){
        return initialized;
    }

    /**
     * Returns the status of the commstream if disconnected
     * @return True if the Tablet has been disconnected.(Not neccesarily hasn't been connected YET)
     */
    public boolean isDisconnected() {return StatusString.contains(Status_Disconnected);}
    /**
     * @ReadStringVersion1: Read String originally simply read the from the inputStream and returned
     *                      a String with a maximum length of 512 Bytes. Multiple threads would call
     *                      this function.
     * @ReadStringVersion2: Due to an error of multiple threads reading from the same stream, only
     *                      one thread(Main Activity) has direct access to the Input File Stream.
     *                      Any data read from the Pi is stored in the String Buffer.
     * @return              The Byte to String data from the raspberry Pi.
     */
    public String readString(){
        //Currently reads the status, will return string
        byte[] readBuffer = new byte[512];

        if(isInitialized() && (mInputStream != null)){
            try{
                 //@TODO Tried the available function
                    ret = mInputStream.read(readBuffer);
                    if (ret > 0) {
                        String s = new String(readBuffer);
                        StoreBuffer(s);//Updated for V2
                        return s;
                    }

            }catch(IOException e){
                e.printStackTrace();
            }catch(NullPointerException npe){
                //CleanUp();//TODO Cleaning up on a null pointer exception.
                npe.printStackTrace();
            }
        }else {
            return null;
        }
        return null;
    }

    /***
     *  Due to the revision of ReadString, read buffer is instead the preferred method to return a
     *  string from the Pi. It also allows storage of strings in case the current activity doesn't
     *  read the current string
     * @return The First String in the Read Buffer (FIFO).
     */
    public static String ReadBuffer(){

        if(readIndex == BUFFER_SIZE){
            readIndex = 0;
        }
        if(readBuffer[readIndex] == null){
            return null;
        }
        //readBuffer[readIndex]=null;//"Popping" message out of buffer

        return readBuffer[readIndex++];
    }

    /**
     * Called by readString, StoreBuffer stores the incoming string in the FIFO Buffer
     * @param in The string obtained from the File Stream
     * @return  True if given a valid string. False if null.
     */
    public static Boolean StoreBuffer(String in){


        if(in==null){return false;}
        if(writeIndex == BUFFER_SIZE){
            //Log.d("CommStream","Creating new buffer");
            writeIndex = 0;
            //readBuffer = new String[BUFFER_SIZE];//DID overwrite same buffer instead of creating new buffer
        }
        //Log.d("CommStream","writeIndex:"+writeIndex+" in:"+in);
            readBuffer[writeIndex] = in;
            writeIndex++;
            return true;

    }

    /**
     * Writes to the output File Stream by converting in input string into an array of bytes
     * written to the output buffer.
     * @param s The String to be sent over the Serial Port
     * @return True if Successful write of String. False if error or invalid string.
     */
    public static boolean writeString(String s) {

        if (mOutputStream != null) {
            if(s == null){return false;}
            //Get clean buffer each time;
            byte[] outBuffer;
            outBuffer = s.getBytes();
            //Writes to output
            try {
                mOutputStream.write(outBuffer);
            } catch (IOException ioe) {

                //Toast.makeText(getApplicationContext(),"Error writing out",Toast.LENGTH_SHORT);
                Log.d("Warn", "Error writing out");
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * IMPORTANT
     * CleanUp() MUST be called when there is a disconnect between the Pi and Tablet. If not the
     * output stream points to a null space in memory and will cause not only the app to crash, but
     * the device to restart.
     */
    public static void CleanUp(){
        if(mOutputStream != null){
            try {
                mOutputStream.close();
                mInputStream.close();
                StatusString = Status_Disconnected;
                parcelFD.close();
            }catch(IOException ioe){
                ioe.printStackTrace();
                Log.d("CommStream","Error Closing OutputStream");
            }
        }
    }


    /**
     *Non-Static Comm Stream File Accessors
     */
    public FileOutputStream getOStream(){
        if(initialized){
            return mOutputStream;
        }else{
            return null;
        }
    }

    public FileInputStream getIStream(){
        if(initialized){
            return mInputStream;
        }else{
            return null;
        }
    }
    public UsbManager getUSB(){
        if(!initialized){
            return null;
        }else{
            return usbMan;
        }
    }
    public UsbAccessory getAcc(){
        if(!initialized){
            return null;
        }else{
            return usbAcc;
        }
    }
    public ParcelFileDescriptor getFDescriptor(){
        if(!initialized){
            return null;
        }else{
            return parcelFD;
        }
    }
    public String ReadStatus(){
        return StatusString;
    }
    public void SetStatus(String s){
        StatusString = s;
    }
    public void PiLog(String... logs){
        for(int i = 0; i < logs.length; i++){
            writeString("$LOG," + logs[i]);
        }
    }


}
