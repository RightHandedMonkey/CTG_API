package com.worxforus.ctg;


public class CTGConstants {
	
	public static final String APK_CODE = "3"; 
	//changed this to private so that getDatabaseName(...) must be called
	//this way each user can have their own database.
	private static final String DATABASE_NAME ="CTG";
		
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

	public static String getDatabaseName(int userNum) {
		return DATABASE_NAME+"_"+userNum;
	}
	
	public static String getOldDatabaseName() { return DATABASE_NAME; }

	
}