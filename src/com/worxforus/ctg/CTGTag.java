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
import com.worxforus.ctg.db.CTGTagTable;
import com.worxforus.ctg.net.CTGNetConstants;
import com.worxforus.json.JSONArrayWrapper;
import com.worxforus.json.JSONExceptionWrapper;
import com.worxforus.json.JSONObjectWrapper;
import com.worxforus.net.SyncInterface;
import com.worxforus.net.SyncTableManager;

public class CTGTag implements SyncInterface<CTGTag>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8935325626521096786L;
	public static final String REV = "0.1";
	public static final String REV_HISTORY0_1 = "Added upload data, uuid, client index";
	public static final String REV_HISTORY0_0 = "Initial Rev";

	//names to be used when parsed in from web
	public static final String ID="id";
	public static final String NAME="name";
	public static final String DESC="desc";
	public static final String PARENT_REF="parent_ref";
	public static final String META_STATUS="meta_status";
	public static final String UPLOAD_DATETIME="upload_datetime";
	public static final String CLIENT_INDEX="client_index";
	public static final String CLIENT_UUID="client_uuid";
	
	//Variables
	protected int id = 0;
	protected String name = "";
	protected String desc = "";
	protected int parent_ref = 0;
	protected int meta_status = CTGConstants.META_STATUS_NORMAL;
	protected String upload_datetime = ""; //this is the date an item is modified - used to determine if item needs to be downloaded to client
	protected int client_index = 0; //index of the item created locally on a device (not the server)
	protected String client_uuid = ""; //unique id of client when item created on a device (client_index and client_uuid should be unique per item created on all remote devices)

	//set to 1 if changed locally, or 0 if changed via web download
	private int locally_changed=0;

	public CTGTag() {
		touch();
	}

    @Override
	public String toString() {
		return "id: "+id+"\nname: "+name+"\ndesc: "+desc+"\nparent_ref: "+parent_ref+"\nmeta_status: "+meta_status;
	}

	/**
     * Activate when changing anything on the item so system knows to upload it when needed
     */
    public void touch() {
    	this.setUpload_datetime(Utils.get_current_datetime_str());
    }
	//========================-----------------------> getters/setters <-----------------=========================\\

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getParent_ref() {
		return parent_ref;
	}
	public void setParent_ref(int parent_ref) {
		this.parent_ref = parent_ref;
	}
	public int getMeta_status() {
		return meta_status;
	}
	public void setMeta_status(int meta_status) {
		this.meta_status = meta_status;
	}
	public String getUpload_datetime() {
		return upload_datetime;
	}
	public void setUpload_datetime(String upload_datetime) {
		this.upload_datetime = upload_datetime;
	}
	public int getClient_index() {
		return client_index;
	}
	public void setClient_index(int client_index) {
		this.client_index = client_index;
	}
	public String getClient_uuid() {
		return client_uuid;
	}
	public void setClient_uuid(String client_uuid) {
		this.client_uuid = client_uuid;
	}

	public int getLocally_changed() {
		return locally_changed;
	}

	public void setLocally_changed(int locally_changed) {
		this.locally_changed = locally_changed;
	}

	public String getDownloadURL(String host) {
		return (host+CTGNetConstants.CTG_TAG_INTERFACE);
	}

	public String getUploadURL(String host) {
		return getDownloadURL(host);
	}

	public boolean requireAuthOnDownload() { return false; }
	public boolean requireAuthOnUpload() { return true; }

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

	public List<NameValuePair> getUploadParams(ArrayList<CTGTag> objects) {
		 List<NameValuePair> params = new ArrayList<NameValuePair>();
		 return fillInObjectParams(objects, params);
	}

	public List<NameValuePair> fillInObjectParams(ArrayList<CTGTag> list, List<NameValuePair> params) {
		String array_str="";
		if (list.size() > 1)
			array_str="[]";
		for (CTGTag tag : list) {
			params.add(new BasicNameValuePair(CTGTagTable.CTG_TAG_ID+array_str, tag.getId()+""));
			params.add(new BasicNameValuePair(CTGTagTable.CTG_TAG_NAME+array_str, tag.getName()+""));
			params.add(new BasicNameValuePair(CTGTagTable.CTG_TAG_DESC+array_str, tag.getDesc()+""));
			params.add(new BasicNameValuePair(CTGTagTable.CTG_PARENT_TAG_REF+array_str, tag.getParent_ref()+""));
			params.add(new BasicNameValuePair(CTGTagTable.CTG_META_STATUS+array_str, tag.getMeta_status()+""));
			params.add(new BasicNameValuePair(CTGTagTable.CTG_TAG_UPLOAD_DATE+array_str, tag.getUpload_datetime()+""));
			params.add(new BasicNameValuePair(CTGTagTable.CTG_TAG_CLIENT_INDEX+array_str, tag.getClient_index()+""));
			params.add(new BasicNameValuePair(CTGTagTable.CTG_TAG_CLIENT_UUID+array_str, tag.getClient_uuid()+""));
		}
		return params;
	}

	/**
	 * TODO: Will eventually need to pass a pool of objects here instead of creating each

	 */
	@Override
	public Result parseJSONtoArrayList(JSONArrayWrapper jsonArr, Pool<CTGTag> pool) {
		Result r = new Result();
		ArrayList<CTGTag> array = new ArrayList<CTGTag>();
		for (int i = 0; i < jsonArr.length(); i++) {
			CTGTag tag = pool.newObject();
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
			setName(obj.getString(NAME));		
			
			String desc =obj.get(DESC).toString(); 
			if (!desc.equals("null"))
				setDesc(desc);

			setParent_ref(obj.getInt(PARENT_REF));
			setMeta_status(obj.getInt(META_STATUS));
			setUpload_datetime(obj.getString(UPLOAD_DATETIME));
			setClient_index(obj.getInt(CLIENT_INDEX));
			setClient_uuid(obj.getString(CLIENT_UUID));
		} catch (JSONExceptionWrapper e) {
			result.technical_error += "Could not parse JSON info:"+e.getMessage();
			result.success = false;
			Log.e(this.getClass().getName(), e.getMessage());
		}
		return result;
	}

	@Override
	public CTGTag createObject() {
		return ( new CTGTag() );
	}

	
	
}
