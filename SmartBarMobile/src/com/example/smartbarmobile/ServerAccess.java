package com.example.smartbarmobile;

/**
 * Static class to hold server access variables.
 * @author lamperry
 *
 */
public class ServerAccess {

	/** SMART BAR ROOT URL**/
	public static final String SMART_BAR_ROOT_URL = "http://smartbar.soe.ucsc.edu/";
	
    /** To log in user **/
    public static final String LOGIN_URL = SMART_BAR_ROOT_URL + "isLogged.php";

    /** To register new user **/
    public static final String REGISTER_URL = SMART_BAR_ROOT_URL + "register.php";

    /** To get user last BAC reading **/
    public static final String BAC_URL = SMART_BAR_ROOT_URL + "getBAC.php";
    
    /** To reset user fingerprint on server **/
    public static final String RESET_FP_URL = SMART_BAR_ROOT_URL + "resetFP.php";

    /** To get library of drinks **/
    public static final String GET_LIB_URL = SMART_BAR_ROOT_URL + "getLib.php";

    /** To put user drink on queue **/
    public static final String ADD_DRINK_URL = SMART_BAR_ROOT_URL + "addDrink.php";

}
