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

/**
 * Created by trider on 2/6/2015.
 */


public class CommStream {

    private static FileInputStream mInputStream;
    private static FileOutputStream mOutputStream;

    private static boolean initialized = false;
    private static UsbAccessory usbAcc;
    private static UsbManager usbMan;
    private static ParcelFileDescriptor parcelFD;

    public static final String Status_Created          =    "UNINITIALIZED_COMM_CREATED";
    public static final String Status_Connected        =    "USB_CONNECTED";
    public static final String Status_Disconnected     =    "USB_DISCONNECTED";

    public static final Boolean ON = true;
    public static final Boolean OFF = false;

    public static String StatusString = "test0";

    private static String[] readBuffer = new String[128];
    private static int readIndex = 0;
    private static int writeIndex = 0;



    public static byte[] writeBuffer = new byte[1024];
    public int ret;

    public static Boolean SYSTEM_FINGERPRINT = ON;
    public static Boolean SYSTEM_BAC         = ON;
    public static Boolean SYSTEM_SERVER      = ON;
    public static Boolean SYSTEM_PI          = ON;


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
        }
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
                ret = mInputStream.read(readBuffer);
                String s = new String(readBuffer);

                StoreBuffer(s);//Updated for V2
                return  s;

            }catch(IOException e){
                e.printStackTrace();
            }catch(NullPointerException npe){
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

        if(readIndex == 1024){
            readIndex = 0;
        }
        if(readBuffer[readIndex] == null){
            return null;
        }
        return readBuffer[readIndex++];
    }

    /**
     * Called by readString, StoreBuffer stores the incoming string in the FIFO Buffer
     * @param in The string obtained from the File Stream
     * @return  True if given a valid string. False if null.
     */
    private Boolean StoreBuffer(String in){
        if(in==null){return false;}
        if(writeIndex == 1024){
            writeIndex = 0;
        }
            readBuffer[writeIndex++] = in;
            return true;

    }


    /**
     * Writes to the output File Stream by converting in input string into an array of bytes
     * written to the output buffer.
     * @param s The String to be sent over the Serial Port
     * @return True if Successful write of String. False if error or invalid string.
     */
    public boolean writeString(String s) {

        if (mOutputStream != null) {
            if(s == null){return false;}
            //Get clean buffer each time;
            byte[] outBuffer;
            outBuffer = s.getBytes();
            //Writes to output
            try {
                mOutputStream.write(outBuffer);
            } catch (IOException ioe) {
                Log.d("Warn", "Error writing out");
                return false;
            }
        }
        return true;
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
            }catch(IOException ioe){
                ioe.printStackTrace();
                Log.d("CommStream","Error Closing OutputStream");
            }
        }
    }


    /**
     *Simple return functions for information about the CommStream
     *
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





}
