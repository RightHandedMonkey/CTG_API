package com.worxforus.ctg.net;

import com.worxforus.ctg.CTGConstants;

public class CTGWebHelper {
	//So testing can be used to mock server
	private static boolean testMode = false;
	private static String testHost = "";
	
	//Test function helpers
	public static String getHost() {
	 if(testMode) {
		 return CTGConstants.CTG_HOST;
	 } else {
		 return CTGWebHelper.testHost;
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
