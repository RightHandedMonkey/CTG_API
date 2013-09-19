package com.worxforus.ctg;


public class CTGConstants {
	
	public static final String APK_CODE = "1"; 
	public static final String DATABASE_NAME ="CTG";
	
	public static final String CTG_HOST = "http://www.checkliststogo.com/";
	public static final String CTG_HOST_HTTPS = "https://www.checkliststogo.com/";
	
	//HTTP Connections
	public static final String CTG_LOGIN = "Ajax/ajax_login_interface.php";
	public static final String CTG_LOGOUT = "cxapp/logout.php";
	
	//User Messages
	public static final String LOGIN_ERROR= "Could not login to server.";

	//Misc global constants
	public static final String PREVIEW_PREFIX = "preview_";


	//Web GET/POST Data request strings
	public static final String CTG_USERID = "lutz_userid";
	public static final String CTG_PASSWORD = "lutz_password";


	//Meta Status Definitions
	public static final int META_STATUS_NORMAL = 0;
	public static final int META_STATUS_DELETED = 1;
	public static final int META_STATUS_TEMPORARY = 2;


	
}