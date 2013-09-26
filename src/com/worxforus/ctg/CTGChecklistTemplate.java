package com.worxforus.ctg;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.worxforus.Pool;
import com.worxforus.Result;
import com.worxforus.Utils;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.ctg.db.CTGChecklistTemplateTable;
import com.worxforus.ctg.net.CTGNetConstants;
import com.worxforus.json.JSONArrayWrapper;
import com.worxforus.json.JSONExceptionWrapper;
import com.worxforus.json.JSONObjectWrapper;
import com.worxforus.net.SyncInterface;
import com.worxforus.net.SyncTableManager;

public class CTGChecklistTemplate implements SyncInterface<CTGChecklistTemplate> {
	public static final String REV = "0.1";
	public static final String REV_HISTORY0_1 = "Added upload data, uuid, client index";
	public static final String REV_HISTORY0_0 = "Initial Rev";

	//names to be used when parsed in from web
	public static final String ID="id";
	public static final String TITLE="title";
	public static final String DESC="desc";
	public static final String META_STATUS="meta_status";
	public static final String BY_USER="by_user";
	public static final String UPLOAD_DATETIME="upload_datetime";
	public static final String SHARED="shared";
	public static final String CAT1_ID="cat1_id";
	public static final String CAT1_NAME="cat1_name";
	public static final String CAT2_ID="cat2_id";
	public static final String CAT2_NAME="cat2_name";
	public static final String CLIENT_INDEX="client_index";
	public static final String CLIENT_UUID="client_uuid";
	
	//Variables
	protected int id = 0;
	protected String title = "";
	protected String desc = "";
	protected int meta_status = CTGConstants.META_STATUS_NORMAL;
	protected int by_user = 0;
	protected String upload_datetime = ""; //this is the date an item is modified - used to determine if item needs to be downloaded to client
	protected int shared = 0; 
	protected int cat1_id = 0; //top level category this template belongs to (for quick searching)
	protected String cat1_name = ""; //top level category this template belongs to (for quick searching)
	protected int cat2_id = 0; //secondary category this template belongs to (for quick searching)
	protected String cat2_name = ""; //secondary category this template belongs to (for quick searching)
	protected int client_index = 0; //index of the item created locally on a device (not the server)
	protected String client_uuid = ""; //unique id of client when item created on a device (client_index and client_uuid should be unique per item created on all remote devices)
	//set to 1 if changed locally, or 0 if changed via web download
	private int locally_changed=0;

	public CTGChecklistTemplate() {
		touch();
	}

    @Override
	public String toString() {
		return "id: "+id+"\ntitle: "+title+"\ndesc: "+desc+"\nby_user: "+by_user+"\nmeta_status: "+meta_status;
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
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

	public int getBy_user() {
		return by_user;
	}

	public void setBy_user(int by_user) {
		this.by_user = by_user;
	}

	public int getShared() {
		return shared;
	}

	public void setShared(int shared) {
		this.shared = shared;
	}

	public int getCat1_id() {
		return cat1_id;
	}

	public void setCat1_id(int cat1_id) {
		this.cat1_id = cat1_id;
	}

	public String getCat1_name() {
		return cat1_name;
	}

	public void setCat1_name(String cat1_name) {
		this.cat1_name = cat1_name;
	}

	public int getCat2_id() {
		return cat2_id;
	}

	public void setCat2_id(int cat2_id) {
		this.cat2_id = cat2_id;
	}

	public String getCat2_name() {
		return cat2_name;
	}

	public void setCat2_name(String cat2_name) {
		this.cat2_name = cat2_name;
	}

	public String getDownloadURL(String host) {
		return (host+CTGNetConstants.CTG_CT_INTERFACE);
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

	public List<NameValuePair> getUploadParams(ArrayList<CTGChecklistTemplate> objects) {
		 List<NameValuePair> params = new ArrayList<NameValuePair>();
		 return fillInObjectParams(objects, params);
	}

	public List<NameValuePair> fillInObjectParams(ArrayList<CTGChecklistTemplate> list, List<NameValuePair> params) {
		String array_str="";
		if (list.size() > 1)
			array_str="[]";
		for (CTGChecklistTemplate item : list) {
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_ID+array_str, item.getId()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_TITLE+array_str, item.getTitle()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_DESC+array_str, item.getDesc()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_META_STATUS+array_str, item.getMeta_status()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_BY_USER+array_str, item.getBy_user()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_UPLOAD_DATE+array_str, item.getUpload_datetime()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_SHARED+array_str, item.getShared()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_CAT1_ID+array_str, item.getCat1_id()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_CAT1_NAME+array_str, item.getCat1_name()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_CAT2_ID+array_str, item.getCat2_id()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_CAT2_NAME+array_str, item.getCat2_name()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_CLIENT_INDEX+array_str, item.getClient_index()+""));
			params.add(new BasicNameValuePair(CTGChecklistTemplateTable.CTG_CT_CLIENT_UUID+array_str, item.getClient_uuid()+""));
		}
		return params;
	}

	/**
	 * TODO: Will eventually need to pass a pool of objects here instead of creating each

	 */
	@Override
	public Result parseJSONtoArrayList(JSONArrayWrapper jsonArr, Pool<CTGChecklistTemplate> pool) {
		Result r = new Result();
		ArrayList<CTGChecklistTemplate> array = new ArrayList<CTGChecklistTemplate>();
		for (int i = 0; i < jsonArr.length(); i++) {
			CTGChecklistTemplate item = pool.newObject();
			try {
				r.add_results_if_error(item.loadJSONObject(jsonArr.getJSONObject(i)), "Could not convert JSON item to an object: "+jsonArr.getJSONObject(i));
				//only add items that were processed - we don't want to add blank items if they could not be found due to exception
				array.add(item);
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
			setTitle(obj.getString(TITLE));		
			
			String desc =obj.get(DESC).toString(); 
			if (!desc.equals("null"))
				setDesc(desc);

			setMeta_status(obj.getInt(META_STATUS));
			setBy_user(obj.getInt(BY_USER));
			setUpload_datetime(obj.getString(UPLOAD_DATETIME));
			setShared(obj.getInt(SHARED));
			setCat1_id(obj.getInt(CAT1_ID));
			setCat1_name(obj.getString(CAT1_NAME));
			setCat2_id(obj.getInt(CAT2_ID));
			setCat2_name(obj.getString(CAT2_NAME));
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
	public CTGChecklistTemplate createObject() {
		return ( new CTGChecklistTemplate() );
	}

	
	
}
