package com.example.trider.smartbarui;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

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


    public static String StatusString = "test0";

    public static byte[] readBuffer = new byte[128];
    public static byte[] writeBuffer = new byte[128];
    public int ret;



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

    public CommStream(){
        //mInputStream = null;
        //mOutputStream = null;
        if(initialized){return;}
        StatusString = Status_Created;
    }

    public CommStream(String s){
        StatusString = s;
    }

    public boolean isInitialized(){
        return initialized;
    }
    public String readString(){
        //Currently reads the status, will return string
         byte[] readBuffer = new byte[128];
        if(isInitialized()){
            try{
                ret  = mInputStream.read(readBuffer);
            }catch(IOException e){
                e.printStackTrace();
            }
        }else {
            return null;
        }
        return new String(readBuffer);
    }
    public boolean writeString(String s) {
        if (mOutputStream != null) {
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
