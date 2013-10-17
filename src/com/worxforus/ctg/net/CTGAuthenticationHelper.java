package com.worxforus.ctg.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.worxforus.Result;
import com.worxforus.json.JSONExceptionWrapper;
import com.worxforus.json.JSONObjectWrapper;
import com.worxforus.net.NetAuthentication;
import com.worxforus.net.NetHandler;
import com.worxforus.net.SyncTableManager;
import com.worxforus.net.NetAuthentication.NetAuthenticationHelper;
import com.worxforus.net.NetResult;


public class CTGAuthenticationHelper implements NetAuthenticationHelper {

	public static final String LOGIN_FAILED = "{\"data\":{\"success\":false,\"error\":\"Could not login user on ChecklistsToGo.com\",\"technical_error\":\"\",\"message\":\"User \'sam.bossen@gmail.com\' could not login.\",\"sql\":\"\",\"last_insert_id\":0,\"last_insert_linked_id\":0,\"last_insert_index\":0,\"string\":\"\",\"object\":null},\"result\":true}";

	@Override
	public String getLoginURL(String host) {
		return (host+CTGNetConstants.CTG_LOGIN_INTERFACE);
	}

	@Override
	public void markAsLoginFailure(NetResult result) {
		try {
			result.object = new JSONObjectWrapper(LOGIN_FAILED);
		} catch (JSONExceptionWrapper e) {
			Log.e(this.getClass().getName(), "Could not parse default login failed json string.");
		}
	}
	
	@Override
	public int checkForLoginError(NetResult netResult) {
		//NetResult.object should contain JsonObjectWrapper
		if (netResult.net_success == true) {
			//check json data to figure out what went wrong, ie. login failed, not logged in (not enough info passed), etc
			JSONObjectWrapper json = ((JSONObjectWrapper) netResult.object);
			//this should be a result object with user in the result.object array
			try {
				//check if we get the data object we are expecting, if not check for invalid auth info
				boolean success = false;
				String error = "";
				if (json.has(CTGNetConstants.CTG_JSON_DATA)) {
					JSONObjectWrapper jsonData = (JSONObjectWrapper) json.getJSONObject(CTGNetConstants.CTG_JSON_DATA);
					//try to get error
					success = jsonData.getBoolean(Result.WEB_SUCCESS);
					error = jsonData.getString(Result.WEB_ERROR);
					
				} else {//try to get error from a different page that doesn't use the 'data' object
					//look at data from 'root' object
					error = json.getString(Result.WEB_ERROR);
				}
				
				if (!success) {
					if (error.contains(CTGNetConstants.NOT_LOGGED_IN)) {
						return NetAuthentication.NOT_LOGGED_IN;
					} else if (error.contains(CTGNetConstants.LOGIN_ERROR)) {
						return NetAuthentication.LOGIN_FAILURE;
					}  
					Log.e(this.getClass().getName(), "Server reported error, but generic login error could not be identified.");
					return NetAuthentication.SERVER_ERROR; //server reported error, not sure what happened
				}
			} catch (JSONExceptionWrapper e) {
				Log.e(this.getClass().getName(), "Could not parse server response.");
				return NetAuthentication.SERVER_ERROR; //not sure why we couldn't log in
			} catch (NullPointerException e) {
				Log.e(this.getClass().getName(), "Could not parse server response.");
				return NetAuthentication.SERVER_ERROR; //not sure why we couldn't log in
			}
			
		} else {
			//login failed, but it was a network error
			return NetAuthentication.NETWORK_ERROR;
		}
		return NetAuthentication.NO_ERRORS;
	}

	@Override
	public NetResult handleUsernameLogin(String host, String username, String password) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(CTGNetConstants.USER_LOGIN_ID, username+""));
		params.add(new BasicNameValuePair(CTGNetConstants.USER_PASSWORD, password+""));
		
		NetResult netResult = NetHandler.handle_post_with_retry(this.getLoginURL(host), params , NetHandler.NETWORK_DEFAULT_RETRY_ATTEMPTS);
		//get json object and close network stream
		NetHandler.handleGenericJsonResponseHelper(netResult, this.getClass().getName());
		Log.d(this.getClass().getName(), "Performed network login to: "+host);
		return netResult;
	}

	@Override
	public NetResult handleTokenLogin(String host, String accessToken, String uuid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
