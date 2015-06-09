package com.example.trider.smartbarui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.trider.smartbarui.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trider on 3/5/2015.
 */
public class ServerAccess {


    public static final String SMART_BAR_ROOT= "http://smartbar.soe.ucsc.edu/";
    public static final String GET_CUST_ID   = SMART_BAR_ROOT+"payment/findCust.php";
    public static final String RECIPE_URL    = SMART_BAR_ROOT+"getDrinkName.php";
    public static final String COUNTER_URL   = SMART_BAR_ROOT+"countDrink.php";
    public static final String GET_TOKEN     = SMART_BAR_ROOT+"jsonToken.php";
    public static final String REGISTER_URL  = SMART_BAR_ROOT+"register.php";
    public static final String LOGIN_URL     = SMART_BAR_ROOT+"isLogged.php";
    public static final String PAY_URL       = SMART_BAR_ROOT+"postBrainID.php";
    public static final String GET_DRINK_URL = SMART_BAR_ROOT+"getDrink.php";
    public static final String ADD_DRINK_URL = SMART_BAR_ROOT+"addDrink.php";
    public static final String RESET_FP      = SMART_BAR_ROOT+"resetFP.php";
    public static final String GET_LIB_URL   = SMART_BAR_ROOT+"getLib.php";
    public static final String PUTBAC_URL    = SMART_BAR_ROOT+"putBAC.php";
    public static final String GETBAC_URL    = SMART_BAR_ROOT+"getBAC.php";
    public static final String GET_QUEUE_URL = SMART_BAR_ROOT+"getQ.php";
    public static final String DELETE_URL    = SMART_BAR_ROOT+"delQ.php";
    public static final String HAS_FP        = SMART_BAR_ROOT+"isFP.php";
    public static final String FIND_PHONE    = SMART_BAR_ROOT+"findPhone.php";
    public static final String GET_INV       = SMART_BAR_ROOT+"getInventory.php";
    public static final String PUT_PRICE     = SMART_BAR_ROOT+"putPrice.php";// drink & price

    public static final String TAG_SUCCESS = "success";
    public static final String TAG_MESSAGE = "message";


}
