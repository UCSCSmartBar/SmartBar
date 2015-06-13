package com.example.smartbarmobile;

/**
 * Static class to hold server access variables.
 * @author lamperry
 */
public class ServerAccess {

    /* JSON element ids from response of php script: */
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_MESSAGE = "message";

	/** SMART BAR ROOT URL**/
	public static final String SMART_BAR_ROOT_URL = "http://smartbar.soe.ucsc.edu/";
	
    /** To log in user **/
    public static final String LOGIN_URL = SMART_BAR_ROOT_URL + "isLogged.php";

    /** To register new user **/
    public static final String REGISTER_URL = SMART_BAR_ROOT_URL + "register.php";
    
    /** To reset user fingerprint on server **/
    public static final String RESET_FP_URL = SMART_BAR_ROOT_URL + "resetFP.php";

    /** To get library of drinks **/
    public static final String GET_LIB_URL = SMART_BAR_ROOT_URL + "getLib.php";

    /** To see if user has drink on queue already **/
    public static final String CHECK_QUEUE_URL = SMART_BAR_ROOT_URL + "getDrinkTitle.php";

    /** To put user drink on queue **/
    public static final String ADD_DRINK_URL = SMART_BAR_ROOT_URL + "addDrink.php";

    /** To get username **/
    public static final String FIND_USER_URL = SMART_BAR_ROOT_URL + "findUser.php";

    /** To get Braintree token **/
    public static final String REQUEST_TOKEN_URL = SMART_BAR_ROOT_URL + "jsonToken.php";

    /** To check if Braintree customer ID exists **/
    public static final String FIND_CUST_URL = SMART_BAR_ROOT_URL + "payment/findCust.php";

    /** To create Braintree customer with payment method **/
    public static final String CREATE_CUST_URL = SMART_BAR_ROOT_URL + "payment/createCust.php";

}
