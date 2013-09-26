package com.worxforus.ctg.net;

public class CTGWebHelper {
	//So testing can be used to mock server
	protected static boolean testMode = false;
	protected static String testHost = "";
	
	//Test function helpers
	public static String getHost() {
	 if(testMode) {
		 return CTGWebHelper.testHost;
	 } else {
		 return CTGNetConstants.CTG_HOST;
	 }
	}
	
	public static void activateTestHost(String _testHost) {
		CTGWebHelper.testMode = true;
		CTGWebHelper.testHost = _testHost;
	}
	
	public static void releaseTestHost() {
		CTGWebHelper.testMode = false;
		CTGWebHelper.testHost = "";
	}
}
