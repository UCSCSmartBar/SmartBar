package com.example.trider.smartbarui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by trider on 2/15/2015.
 */
public class DetectUSB extends BroadcastReceiver {

        private static final String TAG = "DetectUSB";
        public static boolean Connection = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            String action = intent.getAction();
            if (action.equalsIgnoreCase( "android.intent.action.UMS_CONNECTED"))
            {
                Toast toast = Toast.makeText(context,"Connected to USB"+action,Toast.LENGTH_LONG);
                toast.show();
                Log.i(TAG, "USB connected.." + action);
                CommStream.CleanUp();
            }else if (intent.getAction().equalsIgnoreCase( "android.intent.action.UMS_DISCONNECTED"))
            {
                Toast toast = Toast.makeText(context,"Disconnected from USB" + action,Toast.LENGTH_LONG);
                toast.show();
                Log.i(TAG, "USB Disconnected.." + action);
                CommStream.CleanUp();
                Connection = false;
            }else if(intent.getAction().equalsIgnoreCase("android.hardware.usb.action.USB_ACCESSORY_DETACHED")){
                Toast toast = Toast.makeText(context,"Disconnected from Accessory" + action,Toast.LENGTH_LONG);
                toast.show();
                CommStream.CleanUp();
                Connection  = false;
                Log.i(TAG, "Um.." + action);
            }else if(UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
            {
                Toast.makeText(context,"Disconnected from USB_ACC"+action, Toast.LENGTH_SHORT).show();
                UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if(accessory != null){
                    CommStream.CleanUp();
                    Connection = false;
                }
            }

        }
    }