package com.example.trider.smartbarui;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public static String StatusString = "EmptyCommStream";
    public static String testString = "test0";

    public CommStream(FileInputStream iStream, FileOutputStream oStream,UsbAccessory uAcc,
                                                  UsbManager uMan,ParcelFileDescriptor PFD){
        if(iStream != null && oStream != null) {
            mInputStream = iStream;
            mOutputStream = oStream;
            usbAcc = uAcc;
            usbMan = uMan;
            parcelFD = PFD;
            initialized = true;
            StatusString = "CommStream initialized with valid USB Accessory Data"+
            iStream.toString()+
            oStream.toString()+
            usbAcc.toString() +
            usbMan.toString() +
            parcelFD.toString();
        }
    }

    public CommStream(){
        //mInputStream = null;
        //mOutputStream = null;
        if(initialized){return;}
        StatusString = "Empty CommStream made";
    }

    public CommStream(String s){
        testString = s;
    }

    public String readString(){
        return testString;
    }
    public void writeString(String s){
        testString = s;
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
    public String Status(){
        return StatusString;
    }
    public boolean isInitialized(){
        return initialized;
    }

}
