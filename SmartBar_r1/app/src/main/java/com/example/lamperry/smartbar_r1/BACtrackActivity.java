package com.example.lamperry.smartbar_r1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import BACtrackAPI.API.BACtrackAPI;
import BACtrackAPI.API.BACtrackAPICallbacks;
import BACtrackAPI.DATech.Constants.Errors;
import BACtrackAPI.Exceptions.BluetoothLENotSupportedException;
import BACtrackAPI.Exceptions.BluetoothNotEnabledException;


public class BACtrackActivity extends Activity {

    private static String TAG = "MainActivity";

    private TextView statusMessageTextView;

    private BACtrackAPI mAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bactrack);

        this.statusMessageTextView = (TextView)this.findViewById(R.id.status_message_text_view_id);

        this.setStatus(R.string.TEXT_DISCONNECTED);

        try {
            mAPI = new BACtrackAPI(this, mCallbacks);
        } catch (BluetoothLENotSupportedException e) {
            e.printStackTrace();
            this.setStatus(R.string.TEXT_ERR_BLE_NOT_SUPPORTED);
        } catch (BluetoothNotEnabledException e) {
            e.printStackTrace();
            this.setStatus(R.string.TEXT_ERR_BT_NOT_ENABLED);
        }
    }

    public void connectNearestClicked(View v) {
        if (mAPI != null) {
            setStatus(R.string.TEXT_CONNECTING);
            mAPI.connectToNearestBreathalyzer();
        }
    }

    public void disconnectClicked(View v) {
        if (mAPI != null) {
            mAPI.disconnect();
        }
    }

    public void getFirmwareVersionClicked(View v) {
        boolean result = false;
        if (mAPI != null) {
            result = mAPI.getFirmwareVersion();
        }
        if (!result)
            Log.e(TAG, "mAPI.getFirmwareVersion() failed");
        else
            Log.d(TAG, "Firmware version requested");
    }

    public void startBlowProcessClicked(View v) {
        boolean result = false;
        if (mAPI != null) {
            result = mAPI.startCountdown();
        }
        if (!result)
            Log.e(TAG, "mAPI.startCountdown() failed");
        else
            Log.d(TAG, "Blow process start requested");
    }

    private void setStatus(int resourceId) {
        this.setStatus(this.getResources().getString(resourceId));
    }

    private void setStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusMessageTextView.setText(String.format("Status:\n%s", message));
            }
        });
    }

    private final BACtrackAPICallbacks mCallbacks = new BACtrackAPICallbacks() {
        @Override
        public void BACtrackConnected() {
            setStatus(R.string.TEXT_CONNECTED);
        }


        @Override
        public void BACtrackDidConnect(String s) {
            setStatus(R.string.TEXT_DISCOVERING_SERVICES);
        }

        @Override
        public void BACtrackDisconnected() {
            setStatus(R.string.TEXT_DISCONNECTED);
        }

        @Override
        public void a() {

        }

        @Override
        public void BACtrackConnectionTimeout() {

        }

        @Override
        public void b() {

        }

        @Override
        public void BACtrackCountdown(int currentCountdownCount) {
            setStatus(getString(R.string.TEXT_COUNTDOWN) + " " + currentCountdownCount);
        }

        @Override
        public void BACtrackStart() {
            setStatus(R.string.TEXT_BLOW_NOW);
        }

        @Override
        public void BACtrackBlow() {
            setStatus(R.string.TEXT_KEEP_BLOWING);
        }

        @Override
        public void BACtrackAnalyzing() {
            setStatus(R.string.TEXT_ANALYZING);
        }

        @Override
        public void BACtrackResults(float measuredBac) {
            setStatus(getString(R.string.TEXT_FINISHED) + " " + measuredBac);
            Intent intent = new Intent(BACtrackActivity.this, DrinkOrderedActivity.class);
            intent.putExtra("bac?", true);
            intent.putExtra("measuredBac", measuredBac);
            startActivity(intent);
        }

        @Override
        public void BACtrackFirmwareVersion(String version) {
            setStatus(getString(R.string.TEXT_FIRMWARE_VERSION) + " " + version);
        }

        @Override
        public void a(int i) {

        }

        @Override
        public void a(byte b) {

        }

        @Override
        public void b(byte b) {

        }

        @Override
        public void a(byte[] bytes) {

        }

        @Override
        public void b(byte[] bytes) {

        }

        @Override
        public void BACtrackError(int errorCode) {
            if (errorCode == Errors.ERROR_BLOW_ERROR)
                setStatus(R.string.TEXT_ERR_BLOW_ERROR);
        }
    };
}
