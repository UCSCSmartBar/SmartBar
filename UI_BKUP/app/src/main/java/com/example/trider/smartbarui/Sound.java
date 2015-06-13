package com.example.trider.smartbarui;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by trider on 3/27/2015.
 */
public class Sound {

    //private static MediaPlayer mp;

    /**
     * Plays a Sound Byte, but needs application context for media player to start.
     *
     * @param appContext
     */
    public static MediaPlayer playImSorry(Context appContext) {
        if (appContext == null) {
            return null;
        }

        MediaPlayer mp;

        mp = MediaPlayer.create(appContext, R.raw.sorrydavid);
        mp.start();
        return mp;
    }

    public static MediaPlayer playGoodMorning(Context appContext) {
        if (appContext == null) {
            return null;
        }

        MediaPlayer mp;

        mp = MediaPlayer.create(appContext, R.raw.goodmorning);
        mp.start();
        return mp;
    }

    public static MediaPlayer playINVUpdated(Context appContext) {
        if (appContext == null) {
            return null;
        }

        MediaPlayer mp;

        mp = MediaPlayer.create(appContext, R.raw.inventory_updated);
        mp.start();
        return mp;


    }

    public static MediaPlayer playHelloLauren(Context appContext) {
        if (appContext == null) {
            return null;
        }

        MediaPlayer mp;

        mp = MediaPlayer.create(appContext, R.raw.hello_lauren);
        mp.start();
        return mp;
    }






    public static MediaPlayer playYouAreDrunk(Context appContext) {
        if (appContext == null) {
            return null;
        }

        MediaPlayer mp;

        mp = MediaPlayer.create(appContext, R.raw.you_are_drunk);
        mp.start();
        return mp;
    }


    public static MediaPlayer playWelcome(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.welcome);
        mp.start();
        return mp;
    }


    public static MediaPlayer playGreeting(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.greetings);
        mp.start();
        return mp;
    }

    public static MediaPlayer playPlaceFinger(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.placefinger);
        mp.start();
        return mp;
    }

    public static MediaPlayer playPlaceCup(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.placecup);
        mp.start();
        return mp;
    }

    public static MediaPlayer playDebugOn(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.debugon);
        mp.start();
        return mp;
    }

    public static MediaPlayer playDebugOff(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.debugoff);
        mp.start();
        return mp;
    }

    public static MediaPlayer playDrinkQ(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.drinkq);
        mp.start();
        return mp;
    }

    public static MediaPlayer playInventory(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.inventory);
        mp.start();
        return mp;
    }

    public static MediaPlayer playRegister(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.please_register);
        mp.start();
        return mp;
    }

    public static MediaPlayer playBreathe(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.please_breathe);
        mp.start();
        return mp;
    }

    public static MediaPlayer playPlaceCupRepeat(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.please_place_cup);
        mp.start();
        return mp;
    }

    public static MediaPlayer playFingerNotFound(Context appContext) {
        if (appContext == null) {
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.finger_not_found);
        mp.start();
        return mp;
    }

    public static MediaPlayer playSuspense(Context appContext) {
        if (appContext == null){
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.suspense);
        mp.start();
        return mp;
    }
    public static MediaPlayer playTequila(Context appContext) {
        if (appContext == null){
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.tequila);
        mp.start();
        return mp;
    }

    public static MediaPlayer playKeepBreathing(Context appContext) {
        if (appContext == null){
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.keep_breathe);
        mp.start();
        return mp;
    }

    public static MediaPlayer playSuccess(Context appContext) {
        if (appContext == null){
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.success);
        mp.start();
        return mp;
    }

    public static MediaPlayer playEnterPhone(Context appContext) {
        if (appContext == null){
            return null;
        }
        MediaPlayer mp;
        mp = MediaPlayer.create(appContext, R.raw.enterphone);
        mp.start();
        return mp;
    }



    public static MediaPlayer playBACNotDetected(Context appContext) {
        if (appContext == null) {
            return null;
        }

        MediaPlayer mp;

        mp = MediaPlayer.create(appContext, R.raw.bac_no_detect);
        mp.start();
        return mp;
    }


}