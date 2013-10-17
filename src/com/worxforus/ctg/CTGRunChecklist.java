package com.worxforus.ctg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.worxforus.Pool;
import com.worxforus.Result;
import com.worxforus.Utils;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.ctg.db.CTGRunChecklistTable;
import com.worxforus.ctg.net.CTGNetConstants;
import com.worxforus.json.JSONArrayWrapper;
import com.worxforus.json.JSONExceptionWrapper;
import com.worxforus.json.JSONObjectWrapper;
import com.worxforus.net.SyncInterface;
import com.worxforus.net.SyncTableManager;

public class CTGRunChecklist implements SyncInterface<CTGRunChecklist>, Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7220782631098459331L;
	public static final String REV = "0.0";
	public static final String REV_HISTORY0_0 = "Initial Rev";

	//names to be used when parsed in from web
	public static final String ID="id";
	public static final String TITLE="title";
	public static final String TEMPLATE_REF="template_ref";
	public static final String META_STATUS ="meta_status";
	public static final String BY_USER ="by_user";
	public static final String UPLOAD_DATETIME ="upload_datetime";
	public static final String NUM_ITEMS="num_items";
	public static final String NUM_COMPLETE="num_complete";
	public static final String CLIENT_REF_INDEX ="client_ref_index";
	public static final String CLIENT_INDEX ="client_index";
	public static final String CLIENT_UUID ="client_uuid";
	
	//Variables
	protected int id = 0;
	protected String title = ""; //Name given to this checklist
	protected int templateRef= 0; //template this checklist was created from
	protected int meta_status = CTGConstants.META_STATUS_NORMAL;
	protected int byUser = 0; //original user who created item
	protected String uploadDatetime = "";  //this is the date an item is modified - used to determine if template needs to be downloaded to client
	//NOTE: Update items below when checklist has tags assigned
	protected int numItems = 0; //Number of items in checklist - for quick getting of percentage complete
	protected int numComplete = 0; //Number of completed items in checklist - for quick getting of percentage complete

	protected int clientRefIndex = 0; //can be a number or zero - will be a number if template was created offline, then server needs to generate the number
	protected int clientIndex = 0; //index of the item created locally on a device (not the server)
	protected String clientUUID = ""; //unique id of client when item created on a device (client_index and client_uuid should be unique per item created on all remote devices)

	//set to 1 if changed locally, or 0 if changed via web download
	private int locally_changed=0;

	public CTGRunChecklist() {
		touch();
	}

    @Override
	public String toString() {
		return "id: "+id+"\ntitle: "+title+"\ntemplateRef: "+templateRef+"\nmeta_status: "+meta_status;
	}

	/**
     * Activate when changing anything on the item so system knows to upload it when needed
     */
    public void touch() {
    	this.setUploadDatetime(Utils.get_current_datetime_str());
    }
	//========================-----------------------> getters/setters <-----------------=========================\\

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTemplateRef() {
		return templateRef;
	}

	public void setTemplateRef(int templateRef) {
		this.templateRef = templateRef;
	}

	public int getMeta_status() {
		return meta_status;
	}

	public void setMeta_status(int meta_status) {
		this.meta_status = meta_status;
	}

	public int getByUser() {
		return byUser;
	}

	public void setByUser(int byUser) {
		this.byUser = byUser;
	}

	public String getUploadDatetime() {
		return uploadDatetime;
	}

	public void setUploadDatetime(String uploadDatetime) {
		this.uploadDatetime = uploadDatetime;
	}

	public int getNumItems() {
		return numItems;
	}

	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}

	public int getNumComplete() {
		return numComplete;
	}

	public void setNumComplete(int numComplete) {
		this.numComplete = numComplete;
	}

	public int getClientRefIndex() {
		return clientRefIndex;
	}

	public void setClientRefIndex(int clientRefIndex) {
		this.clientRefIndex = clientRefIndex;
	}

	public int getClientIndex() {
		return clientIndex;
	}

	public void setClientIndex(int clientIndex) {
		this.clientIndex = clientIndex;
	}

	public String getClientUUID() {
		return clientUUID;
	}

	public void setClientUUID(String clientUUID) {
		this.clientUUID = clientUUID;
	}

	public int getLocally_changed() {
		return locally_changed;
	}

	public void setLocally_changed(int locally_changed) {
		this.locally_changed = locally_changed;
	}

	public String getDownloadURL(String host) {
		return (host+CTGNetConstants.CTG_RC_INTERFACE);
	}

	public String getUploadURL(String host) {
		return getDownloadURL(host);
	}

/**
 * Prepares parameters for sending to webserver.
 * NOTE on first call, we don't know what the toDate is because we get that from server
 * all subsequent calls should resend the toDate to the server to prevent the device getting out of sync with the databse
 */
	public List<NameValuePair> getDownloadParams(int selPage, int limitPerPage, String lastSync, String toDate) {
	     //if nothing passed, do not send pair
		 List<NameValuePair> params = new ArrayList<NameValuePair>();
		 params.add(new BasicNameValuePair(SyncTableManager.ITEM_LIMIT_PER_PAGE, limitPerPage+""));
		 params.add(new BasicNameValuePair(SyncTableManager.SELECTED_PAGE, selPage+""));
		 params.add(new BasicNameValuePair(SyncTableManager.FROM_DATETIME, lastSync+""));
		 if (toDate.length() > 0)
		 params.add(new BasicNameValuePair(SyncTableManager.TO_DATETIME, toDate+""));
		return params;
	}

	public List<NameValuePair> getUploadParams(ArrayList<CTGRunChecklist> objects) {
		 List<NameValuePair> params = new ArrayList<NameValuePair>();
		 return fillInObjectParams(objects, params);
	}

	public List<NameValuePair> fillInObjectParams(ArrayList<CTGRunChecklist> list, List<NameValuePair> params) {
		String array_str="";
		if (list.size() > 1)
			array_str="[]";
		for (CTGRunChecklist tag : list) {
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_ID+array_str, tag.getId()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_TITLE+array_str, tag.getTitle()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_TEMPLATE_REF+array_str, tag.getTemplateRef()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_META_STATUS+array_str, tag.getMeta_status()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_BY_USER+array_str, tag.getByUser()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_UPLOAD_DATE+array_str, tag.getUploadDatetime()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_NUM_ITEMS+array_str, tag.getNumItems()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_NUM_COMPLETE+array_str, tag.getNumComplete()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_CLIENT_REF_INDEX+array_str, tag.getClientRefIndex()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_CLIENT_INDEX+array_str, tag.getClientIndex()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_CLIENT_UUID+array_str, tag.getClientUUID()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistTable.CTG_RC_LOCALLY_CHANGED+array_str, tag.getLocally_changed()+""));
		}
		return params;
	}

	/**
	 * TODO: Will eventually need to pass a pool of objects here instead of creating each

	 */
	@Override
	public Result parseJSONtoArrayList(JSONArrayWrapper jsonArr, Pool<CTGRunChecklist> pool) {
		Result r = new Result();
		ArrayList<CTGRunChecklist> array = new ArrayList<CTGRunChecklist>();
		for (int i = 0; i < jsonArr.length(); i++) {
			CTGRunChecklist tag = pool.newObject();
			try {
				r.add_results_if_error(tag.loadJSONObject(jsonArr.getJSONObject(i)), "Could not convert JSON item to an object: "+jsonArr.getJSONObject(i));
				//only add items that were processed - we don't want to add blank items if they could not be found due to exception
				array.add(tag);
			} catch (JSONExceptionWrapper e) {
				//could not find JSON item in JSON array
				r.success = false;
				r.add_error("Could not find object#"+i+" in JSON Array", false);
			}
		}
		r.object = array;
		return r;
	}

	public Result loadJSONObject(JSONObjectWrapper obj) {
		Result result = new Result();
		try {
			setLocally_changed(0); //allows set to not changed locally when importing from a JSON object
			setId(obj.getInt(ID));
			String tmp =obj.get(TITLE).toString(); 
			if (!tmp.equals("null"))
				setTitle(tmp);
			setTemplateRef(obj.getInt(TEMPLATE_REF));		
			setMeta_status(obj.getInt(META_STATUS));
			setByUser(obj.getInt(BY_USER));
			tmp =obj.get(UPLOAD_DATETIME).toString(); 
			if (!tmp.equals("null"))
				setUploadDatetime(tmp);
			setNumItems(obj.getInt(NUM_ITEMS));
			setNumComplete(obj.getInt(NUM_COMPLETE));
			setClientRefIndex(obj.getInt(CLIENT_REF_INDEX));
			setClientIndex(obj.getInt(CLIENT_INDEX));
			setClientUUID(obj.getString(CLIENT_UUID));

		} catch (JSONExceptionWrapper e) {
			result.technical_error += "Could not parse JSON info:"+e.getMessage();
			result.success = false;
			Log.e(this.getClass().getName(), e.getMessage());
		}
		return result;
	}

	@Override
	public CTGRunChecklist createObject() {
		return ( new CTGRunChecklist() );
	}

	
	
}
