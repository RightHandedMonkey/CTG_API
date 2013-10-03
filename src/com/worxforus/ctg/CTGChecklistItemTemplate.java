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
import com.worxforus.ctg.db.CTGChecklistItemTemplateTable;
import com.worxforus.ctg.net.CTGNetConstants;
import com.worxforus.json.JSONArrayWrapper;
import com.worxforus.json.JSONExceptionWrapper;
import com.worxforus.json.JSONObjectWrapper;
import com.worxforus.net.SyncInterface;
import com.worxforus.net.SyncTableManager;

public class CTGChecklistItemTemplate implements SyncInterface<CTGChecklistItemTemplate>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8682677728672070055L;

	public static final String REV = "0.0";
	public static final String REV_HISTORY0_0 = "Initial Rev";

	//names to be used when parsed in from web
	public static final String ID="id";
	public static final String TEMPLATE_REF="tr";
	public static final String QUESTION="q";
	public static final String TYPE="t";
	public static final String EXTRA="e";
	public static final String SECTION_ORDER="so";
	public static final String SECTION_INDEX="si";
	public static final String SECTION_NAME ="sn";
	public static final String META_STATUS ="ms";
	public static final String BY_USER ="bu";
	public static final String UPLOAD_DATE ="ud";
	public static final String CLIENT_REF_INDEX ="cri";
	public static final String CLIENT_INDEX ="ci";
	public static final String CLIENT_UUID ="cu";
	
	//Variables
	protected int id = 0;
	protected int templateRef= 0;
	protected String question = "";
	protected String type = "";
	protected String extra = "";
	protected int sectionOrder = 0;
	protected int sectionIndex = 0;
	protected String sectionName = "";
	protected int meta_status = CTGConstants.META_STATUS_NORMAL;
	protected int byUser = 0;
	protected String uploadDatetime = ""; //this is the date an item is modified - used to determine if item needs to be downloaded to client
	protected int clientRefIndex = 0; //client ref index - index of the template created locally on a device (not the server)
	protected int clientIndex = 0; //index of the item created locally on a device (not the server)
	protected String clientUUID = ""; //unique id of client when item created on a device (client_index and client_uuid should be unique per item created on all remote devices)

	//set to 1 if changed locally, or 0 if changed via web download
	private int locally_changed=0;

	public CTGChecklistItemTemplate() {
		touch();
	}

    @Override
	public String toString() {
		return "id: "+id+"\nquestion: "+question+"\ntype: "+type+"\ntemplateRef: "+templateRef+"\nmeta_status: "+meta_status;
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

	public int getTemplateRef() {
		return templateRef;
	}

	public void setTemplateRef(int templateRef) {
		this.templateRef = templateRef;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public int getSectionOrder() {
		return sectionOrder;
	}

	public void setSectionOrder(int sectionOrder) {
		this.sectionOrder = sectionOrder;
	}

	public int getSectionIndex() {
		return sectionIndex;
	}

	public void setSectionIndex(int sectionIndex) {
		this.sectionIndex = sectionIndex;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
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
		return (host+CTGNetConstants.CTG_CIT_INTERFACE);
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

	public List<NameValuePair> getUploadParams(ArrayList<CTGChecklistItemTemplate> objects) {
		 List<NameValuePair> params = new ArrayList<NameValuePair>();
		 return fillInObjectParams(objects, params);
	}

	public List<NameValuePair> fillInObjectParams(ArrayList<CTGChecklistItemTemplate> list, List<NameValuePair> params) {
		String array_str="";
		if (list.size() > 1)
			array_str="[]";
		for (CTGChecklistItemTemplate tag : list) {
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_ID+array_str, tag.getId()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_TEMPLATE_REF+array_str, tag.getTemplateRef()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_QUESTION+array_str, tag.getQuestion()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_TYPE+array_str, tag.getType()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_EXTRA+array_str, tag.getExtra()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_SECTION_ORDER+array_str, tag.getSectionOrder()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_SECTION_INDEX+array_str, tag.getSectionIndex()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_SECTION_NAME+array_str, tag.getSectionName()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_META_STATUS+array_str, tag.getMeta_status()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_BY_USER+array_str, tag.getByUser()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_UPLOAD_DATE+array_str, tag.getUploadDatetime()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_CLIENT_REF_INDEX+array_str, tag.getClientRefIndex()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_CLIENT_INDEX+array_str, tag.getClientIndex()+""));
			params.add(new BasicNameValuePair(CTGChecklistItemTemplateTable.CTG_CIT_CLIENT_UUID+array_str, tag.getClientUUID()+""));
		}
		return params;
	}

	/**
	 * TODO: Will eventually need to pass a pool of objects here instead of creating each

	 */
	@Override
	public Result parseJSONtoArrayList(JSONArrayWrapper jsonArr, Pool<CTGChecklistItemTemplate> pool) {
		Result r = new Result();
		ArrayList<CTGChecklistItemTemplate> array = new ArrayList<CTGChecklistItemTemplate>();
		for (int i = 0; i < jsonArr.length(); i++) {
			CTGChecklistItemTemplate tag = pool.newObject();
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
			setTemplateRef(obj.getInt(TEMPLATE_REF));		
			
			String tmp =obj.get(QUESTION).toString(); 
			if (!tmp.equals("null"))
				setQuestion(tmp);
			tmp =obj.get(TYPE).toString(); 
			if (!tmp.equals("null"))
				setType(tmp);
			tmp =obj.get(EXTRA).toString(); 
			if (!tmp.equals("null"))
				setExtra(tmp);

			setSectionOrder(obj.getInt(SECTION_ORDER));
			setSectionIndex(obj.getInt(SECTION_INDEX));

			tmp =obj.get(SECTION_NAME).toString(); 
			if (!tmp.equals("null"))
				setSectionName(tmp);

			setMeta_status(obj.getInt(META_STATUS));
			setByUser(obj.getInt(BY_USER));
			setUploadDatetime(obj.getString(UPLOAD_DATE));
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
	public CTGChecklistItemTemplate createObject() {
		return ( new CTGChecklistItemTemplate() );
	}

	
	
}
