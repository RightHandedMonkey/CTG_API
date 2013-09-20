package com.worxforus.ctg;

import com.worxforus.Utils;
import com.worxforus.ctg.CTGConstants;

public class CTGTag {
	//db definitions
//	public static final String CTG_TAG_TABLE = "ctg_tag_table";
//	public static final String CTG_TAG_ID = "ctg_tag_id"; //int
//	public static final String CTG_TAG_NAME = "ctg_tag_name"; //varchar(256)
//	public static final String CTG_TAG_DESC = "ctg_tag_desc"; //text
//	public static final String CTG_PARENT_TAG_REF = "ctg_parent_tag_ref"; //int
//	public static final String CTG_META_STATUS = "ctg_meta_status"; //int
	public static final String REV = "0.1";
	public static final String REV_HISTORY0_1 = "Added upload data, uuid, client index";
	public static final String REV_HISTORY0_0 = "Initial Rev";
//	public static final String CTG_TAG_UPLOAD_DATE = "ctg_tag_upload_date"; //DATETIME
//	//synchronization items
//	public static final String CTG_TAG_CLIENT_INDEX = "ctg_tag_client_index"; //INT
//	public static final String CTG_TAG_CLIENT_UUID = "ctg_tag_client_uuid"; //VARCHAR([UUID_SIZE])
//	public static final int MAX_NAME_LENGTH = 256;

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

	
	
}
