package com.worxforus.ctg.net;

/**
 * This class contains the web constants to use for doing GET/POST calls
 * to the web service.
 * NOTE: Keep synchronized with ctg/app/model/ctg_web_constants.php
 * @author sbossen
 *
 */
public class CTGNetConstants {
	public static final String CTG_HOST = "http://www.checkliststogo.com/";
	public static final String CTG_HOST_HTTPS = "https://www.checkliststogo.com/";
	
	//Web Connections
	public static final String CTG_TAG_INTERFACE = "ctg/app/service/android_tag_interface.php";
	public static final String CTG_RCI_INTERFACE = "ctg/app/service/android_rci_interface.php";
	public static final String CTG_RC_INTERFACE = "ctg/app/service/android_rc_interface.php";
	public static final String CTG_CT_INTERFACE = "ctg/app/service/android_ct_interface.php";
	public static final String CTG_CIT_INTERFACE = "ctg/app/service/android_cit_interface.php";
	public static final String CTG_LOGIN_INTERFACE = "ctg/app/service/ajax_login_interface.php";
	public static final String CTG_LOGOUT = "ctg/app/logout.php";
	

	//Generic Json Data field names
	public static final String CTG_JSON_DATA = "data"; 
	
	//user login fields
	public static final String USER_LOGIN_ID= "user_login_id";
	public static final String USER_PASSWORD= "user_password";

	//Login error constants
	public static final String NOT_LOGGED_IN = "Please login to access your items";
	public static final String LOGIN_ERROR = "Could not login user";

	public static int DEFAULT_ITEMS_TO_SEND = 100; //This is the number of items sent via the network
}