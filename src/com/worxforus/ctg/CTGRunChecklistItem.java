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
import com.worxforus.ctg.db.CTGRunChecklistItemTable;
import com.worxforus.ctg.net.CTGNetConstants;
import com.worxforus.json.JSONArrayWrapper;
import com.worxforus.json.JSONExceptionWrapper;
import com.worxforus.json.JSONObjectWrapper;
import com.worxforus.net.SyncInterface;
import com.worxforus.net.SyncTableManager;

public class CTGRunChecklistItem implements SyncInterface<CTGRunChecklistItem>, Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3385060499189942445L;
	
	public static final String REV = "0.0";
	public static final String REV_HISTORY0_0 = "Initial Rev";

	//names to be used when parsed in from web
	public static final String ID="id";
	public static final String RUN_CHECKLIST_REF="rcr";
	public static final String CHECKLIST_ITEM_TEMPLATE_REF="citr";
	public static final String QUESTION="q";
	public static final String TYPE="t";
	public static final String EXTRA="e";
	public static final String SECTION_ORDER="so";
	public static final String SECTION_INDEX="si";
	public static final String SECTION_NAME ="sn";
	public static final String META_STATUS ="ms";
	public static final String BY_USER ="bu";
	public static final String UPLOAD_DATE ="ud";
	public static final String VALUE ="v";
	public static final String COMMENT ="c";
	public static final String CLIENT_RC_REF_INDEX ="crri";
	public static final String CLIENT_CIT_REF_INDEX ="ccri";
	public static final String CLIENT_INDEX ="ci";
	public static final String CLIENT_UUID ="cu";
	
	//Variables
	protected int id = 0;
	protected int runChecklistRef= 0;//run checklist ref this item belongs to
	protected int checklistItemTemplateRef = 0;//checklist item template ref this item belongs to
	protected String question ="";//question - Item question
	protected String type = "";//type - This identifies the type of question being asked ie. Boolean, ennumeration, text, etc (Future use)
	protected String extra = "";//extra - To further specify question ie. list of ennumerated values, specification of units, etc.
	protected int sectionOrder = 0;//section order - specifies where the item is in the order for the section that it is in
	protected int sectionIndex = 0;//section index - number of the section in which the item appears
	protected String sectionName = ""; //section name of the section the item is in
	protected int meta_status = CTGConstants.META_STATUS_NORMAL;//0 is normal, 1 is removed, 2 is temporary
	protected int byUser = 0;//by user - original user who created item
	protected String uploadDatetime = ""; //this is the date an item is modified - used to determine if item needs to be downloaded to client

	protected String value = ""; //value - status/result of the checklist item - 0 or 1 for unchecked/checked
	protected String comment = "";
	
	protected int clientRunChecklistRefIndex = 0;//client run checklist ref index - when created locally linked to a run checklist that hasn't been uploaded yet
	protected int clientChecklistItemTemplateRefIndex = 0; //client checklist item template ref index - when created locally linked to a checklist template item that hasn't been uploaded yet
	protected int clientIndex = 0; //index of the item created locally on a device (not the server)
	protected String clientUUID = ""; //unique id of client when item created on a device (client_index and client_uuid should be unique per item created on all remote devices)
	//set to 1 if changed locally, or 0 if changed via web download
	private int locally_changed=0;

	public CTGRunChecklistItem() {
		touch();
	}

    @Override
	public String toString() {
		return "id: "+id+"\nquestion: "+question+"\ntype: "+type+"\nrunChecklistRef: "+runChecklistRef+"\nitemTemplateRef: "+checklistItemTemplateRef+"\nmeta_status: "+meta_status;
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

	public int getClientRunChecklistRefIndex() {
		return clientRunChecklistRefIndex;
	}

	public void setClientRunChecklistRefIndex(int clientRunChecklistRefIndex) {
		this.clientRunChecklistRefIndex = clientRunChecklistRefIndex;
	}

	public int getClientChecklistItemTemplateRefIndex() {
		return clientChecklistItemTemplateRefIndex;
	}

	public void setClientChecklistItemTemplateRefIndex(int clientChecklistItemTemplateRefIndex) {
		this.clientChecklistItemTemplateRefIndex = clientChecklistItemTemplateRefIndex;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRunChecklistRef() {
		return runChecklistRef;
	}

	public void setRunChecklistRef(int runChecklistRef) {
		this.runChecklistRef = runChecklistRef;
	}

	public int getChecklistItemTemplateRef() {
		return checklistItemTemplateRef;
	}

	public void setChecklistItemTemplateRef(int checklistItemTemplateRef) {
		this.checklistItemTemplateRef = checklistItemTemplateRef;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUploadDatetime() {
		return uploadDatetime;
	}

	public void setUploadDatetime(String uploadDatetime) {
		this.uploadDatetime = uploadDatetime;
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
		return (host+CTGNetConstants.CTG_RCI_INTERFACE);
	}
	
	public String getUploadURL(String host) {
		return getDownloadURL(host);
	}

	public boolean requireAuthOnDownload() { return true; }
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

	public List<NameValuePair> getUploadParams(ArrayList<CTGRunChecklistItem> objects) {
		 List<NameValuePair> params = new ArrayList<NameValuePair>();
		 return fillInObjectParams(objects, params);
	}

	/**
	 * Used for uploading data
	 */
	public List<NameValuePair> fillInObjectParams(ArrayList<CTGRunChecklistItem> list, List<NameValuePair> params) {
		String array_str="";
		if (list.size() > 1)
			array_str="[]";
		for (CTGRunChecklistItem tag : list) {
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_ID+array_str, tag.getId()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_RUN_CHECKLIST_REF+array_str, tag.getRunChecklistRef()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_CHECKLIST_ITEM_TEMPLATE_REF+array_str, tag.getChecklistItemTemplateRef()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_QUESTION+array_str, tag.getQuestion()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_TYPE+array_str, tag.getType()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_EXTRA+array_str, tag.getExtra()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_SECTION_ORDER+array_str, tag.getSectionOrder()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_SECTION_INDEX+array_str, tag.getSectionIndex()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_SECTION_NAME+array_str, tag.getSectionName()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_META_STATUS+array_str, tag.getMeta_status()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_BY_USER+array_str, tag.getByUser()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_UPLOAD_DATE+array_str, tag.getUploadDatetime()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_VALUE+array_str, tag.getValue()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_COMMENT+array_str, tag.getComment()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_CLIENT_RC_REF_INDEX+array_str, tag.getClientRunChecklistRefIndex()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_CLIENT_CIT_REF_INDEX+array_str, tag.getClientChecklistItemTemplateRefIndex()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_CLIENT_INDEX+array_str, tag.getClientIndex()+""));
			params.add(new BasicNameValuePair(CTGRunChecklistItemTable.CTG_RCI_CLIENT_UUID+array_str, tag.getClientUUID()+""));
			//locally changed is not an upload parameter so it doesn't get added here
		}
		return params;
	}

	/**
	 * TODO: Will eventually need to pass a pool of objects here instead of creating each

	 */
	@Override
	public Result parseJSONtoArrayList(JSONArrayWrapper jsonArr, Pool<CTGRunChecklistItem> pool) {
		Result r = new Result();
		ArrayList<CTGRunChecklistItem> array = new ArrayList<CTGRunChecklistItem>();
		for (int i = 0; i < jsonArr.length(); i++) {
			CTGRunChecklistItem tag = pool.newObject();
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
			setRunChecklistRef(obj.getInt(RUN_CHECKLIST_REF));		
			setChecklistItemTemplateRef(obj.getInt(CHECKLIST_ITEM_TEMPLATE_REF));		
			
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

			tmp =obj.get(VALUE).toString(); 
			if (!tmp.equals("null"))
				setValue(tmp);
			tmp =obj.get(COMMENT).toString(); 
			if (!tmp.equals("null"))
				setComment(tmp);
			setClientRunChecklistRefIndex(obj.getInt(CLIENT_RC_REF_INDEX));
			setClientChecklistItemTemplateRefIndex(obj.getInt(CLIENT_CIT_REF_INDEX));
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
	public CTGRunChecklistItem createObject() {
		return ( new CTGRunChecklistItem() );
	}

	
	
}
