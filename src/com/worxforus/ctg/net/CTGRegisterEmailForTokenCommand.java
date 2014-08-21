package com.worxforus.ctg.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import junit.framework.Assert;
import android.content.Context;
import android.util.Log;

import com.worxforus.Command;
import com.worxforus.Result;
import com.worxforus.SyncEntry;
import com.worxforus.Utils;
import com.worxforus.app.CITListNotifier;
import com.worxforus.app.CTListNotifier;
import com.worxforus.app.RCIListNotifier;
import com.worxforus.app.RCListNotifier;
import com.worxforus.app.TagListNotifier;
import com.worxforus.ctg.CTGChecklistItemTemplate;
import com.worxforus.ctg.CTGChecklistTemplate;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.ctg.CTGRunChecklist;
import com.worxforus.ctg.CTGRunChecklistItem;
import com.worxforus.ctg.CTGTablePool;
import com.worxforus.ctg.CTGTag;
import com.worxforus.ctg.TablePool;
import com.worxforus.ctg.db.CTGChecklistItemTemplateTable;
import com.worxforus.ctg.db.CTGChecklistTemplateTable;
import com.worxforus.ctg.db.CTGRunChecklistItemTable;
import com.worxforus.ctg.db.CTGRunChecklistTable;
import com.worxforus.ctg.db.CTGTagTable;
import com.worxforus.db.TableManager;
import com.worxforus.json.JSONExceptionWrapper;
import com.worxforus.json.JSONObjectWrapper;
import com.worxforus.net.NetAuthentication;
import com.worxforus.net.NetHandler;
import com.worxforus.net.NetResult;
import com.worxforus.net.SyncTableManager;

public class CTGRegisterEmailForTokenCommand implements Command {

	public static final String MESSAGE_ABORT_SYNC = "Operation has received an abort command.";
	private volatile boolean abort = false;
	private Context c;
	private String email;
	private String hashed_uuid;
	private volatile int state = Command.STATE_WAITING;

	public CTGRegisterEmailForTokenCommand(Context c, String emailToRegister, String hashed_uuid) {
		this.c = c;
		this.email = emailToRegister;
		this.hashed_uuid = hashed_uuid;
	}

	@Override
	public synchronized Result execute() {
		state = Command.STATE_RUNNING;
		long startTime = System.nanoTime() / 1000000;
		// Clear the sync db
		// do any other needed cleanup to reset for a different user
		Assert.assertNotNull(c);

		Result r = new Result();
		if (NetHandler.isNetworkConnected(c)) { // check if the network is ready
												// to handle a request
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(CTGNetConstants.USER_LOGIN_ID, email));
			params.add(new BasicNameValuePair(CTGNetConstants.CTG_TOKEN_UUID_HASH, hashed_uuid));

			if (checkAbort(r)) {
				return r;
			}

			Utils.LogD(this.getClass().getName(), "Starting CTG Email check for account creation.");
			NetResult netResult = NetHandler.handlePostWithRetry(CTGNetConstants.CTG_HOST+CTGNetConstants.CTG_REGISTER_EMAIL, params,
					NetHandler.NETWORK_DEFAULT_RETRY_ATTEMPTS);
			if (netResult.net_success) {
				Result parseObject =NetHandler.handleGenericJsonResponseHelper(netResult, this.getClass().getName());
				if (parseObject.success) {
					r.object = parseObject.object;
					JSONObjectWrapper json = (JSONObjectWrapper) r.object;
					try {
						//error will not be present when successful
						if (json.has(CTGNetConstants.CTG_PAGE_ERROR))
							r.error = json.getString(CTGNetConstants.CTG_PAGE_ERROR);
						//message will not be present when failing
						if (json.has(CTGNetConstants.CTG_PAGE_MESSAGE))
							r.message = json.getString(CTGNetConstants.CTG_PAGE_MESSAGE);
						//the result should always exist, we will get an exception if it does not appear
						r.success = json.getBoolean(CTGNetConstants.CTG_PAGE_RESULT);
						
						//token will be available if successful
						if (json.has(CTGNetConstants.CTG_TOKEN)) {
							r.string = json.getString(CTGNetConstants.CTG_TOKEN);
						} else {
							//No token was returned, so task was not successful - server should have returned a message
							r.success = false;
						}
						
					} catch (JSONExceptionWrapper e) {
						r.success = false;
						r.error = "Did not get expected result from server.";
						r.technical_error = e.getMessage();
						Log.e(this.getClass().getName(), e.getMessage());
					}
				} else {
					r.success = false;
					r.error = "Could not read response from server.";
				}
			} else {
				r.success = false; 
				r.error = "Could not communicate with server.";
			}
		}

		long finishTime = System.nanoTime() / 1000000;
		Utils.LogD(this.getClass().getName(), "Finish CTG email registration request - took "
				+ (finishTime - startTime) + " ms");
		Utils.LogD(this.getClass().getName(), "Result was: "+r);
		state = Command.STATE_FINISHED;

		return r;

	}

	private boolean checkAbort(Result r) {
		if (abort) {
			r.success = false;
			r.technical_error = MESSAGE_ABORT_SYNC;
			state = Command.STATE_FINISHED;
			return true;
		}
		return false;
	}

	@Override
	public void abort() {
		this.abort = true;
	}

	@Override
	public synchronized void attach(Context c) {
		this.c = c;
	}

	@Override
	public synchronized void release() {
		this.c = null;
	}

	@Override
	public int getState() {
		return state;
	}

}
