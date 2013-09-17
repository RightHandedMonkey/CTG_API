package com.worxforus.ctg.net;

import com.worxforus.net.NetResult;

public class CTGAuthentication {
	
	private String username="";
	private String password="";
	private String accessToken=""; //accessToken can be used instead of a password
	private volatile boolean isLoggedIn = false; //store if we have been authenticated
	private long lastLoginTime = 0; //stores last time we authenticated
	
	public static long LOGIN_REFRESH_TIME_SECONDS = 28800; //8 hours
    // Private constructor prevents instantiation from other classes
   private CTGAuthentication() { }

   /**
   * SingletonHolder is loaded on the first execution of Singleton.getInstance()
   * or the first access to SingletonHolder.INSTANCE, not before.
   */

   private static class CTGAuthenticationHolder {
           public static final CTGAuthentication INSTANCE = new CTGAuthentication();
   }

   public static CTGAuthentication getInstance() {
           return CTGAuthenticationHolder.INSTANCE;
   }
   
   public static void loadUsername(String user) {
	   CTGAuthentication.getInstance().username = user;   
   }
   
   public static void loadPassword(String pass) {
	   CTGAuthentication.getInstance().password = pass;   
   }

   public static void loadAccessToken(String token) {
	 CTGAuthentication.getInstance().accessToken = token;   
   }
   
   /**
    * Use this function after calling loadUsername and loadPassword
    * Run inside a separate thread using Runnable or ASyncTask since the network calls may block for a while
    * 
    * The NetHelper interface is used to attempt access multiple retries if needed 
    * @return NetResult - NetResult.net_success tells if the login worked or not
    */
   public static NetResult authenticateViaUsernamePassword() {
	   NetResult result = new NetResult();
	   result.net_success = false;
	   if (result.net_success) {
		   CTGAuthentication.getInstance().lastLoginTime = System.nanoTime();
		   CTGAuthentication.getInstance().isLoggedIn = true;
		   
	   }
	   return result;
   }

   /**
    * Returns true if the cache value looks ok and we think we are still logged in.
    * Returns false if not.
    */
   protected static boolean isCurrentAuthenticationValid() {
	   if (CTGAuthentication.getInstance().isLoggedIn) {
		   long time_diff_nano = System.nanoTime() - CTGAuthentication.getInstance().lastLoginTime;
		   long time_diff_sec = time_diff_nano/1000000000; //convert nanoseconds to seconds: nano/micro/milli
		   if (time_diff_sec < CTGAuthentication.LOGIN_REFRESH_TIME_SECONDS)
			   return true;
		   else
			   return false;
	   }
	   return false;
   }
   
   /**
    * Mark logged in cache as invalid - so it will be rechecked next time authenticate is called
    */
   public static void invalidate() {
	   CTGAuthentication.getInstance().isLoggedIn = false;
   }
}
