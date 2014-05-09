package com.worxforus.ctg.db;

import java.util.ArrayList;

import junit.framework.Assert;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.worxforus.Result;
import com.worxforus.Utils;
import com.worxforus.ctg.CTGChecklistItemTemplate;
import com.worxforus.ctg.CTGChecklistTemplate;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.ctg.CTGRunChecklistItem;
import com.worxforus.db.TableInterface;

public class CTGChecklistItemTemplateTable extends TableInterface<CTGChecklistItemTemplate> {

	public static final String DATABASE_NAME = CTGConstants.DATABASE_NAME;
	public static final String DATABASE_TABLE = "ctg_checklist_item_template_table";
	public static final int TABLE_VERSION = 1;
	// 1 - Initial version

	static int i = 0; // counter for field index
	public static final String CTG_CIT_ID = "ctg_cit_id"; // int
	public static final int CTG_CIT_ID_COL = i++;
	public static final String CTG_CIT_TEMPLATE_REF = "ctg_cit_template_ref"; // varchar(256)
	public static final int CTG_CIT_TEMPLATE_REF_COL = i++;
	public static final String CTG_CIT_QUESTION = "ctg_cit_question"; // text
	public static final int CTG_CIT_QUESTION_COL = i++;
	public static final String CTG_CIT_TYPE = "ctg_cit_type"; // int
	public static final int CTG_CIT_TYPE_COL = i++;
	public static final String CTG_CIT_EXTRA = "ctg_cit_extra"; // int
	public static final int CTG_CIT_EXTRA_COL = i++;
	public static final String CTG_CIT_SECTION_ORDER = "ctg_cit_section_order"; // TEXT
	public static final int CTG_CIT_SECTION_ORDER_COL = i++;
	public static final String CTG_CIT_SECTION_INDEX = "ctg_cit_section_index"; // INT
	public static final int CTG_CIT_SECTION_INDEX_COL = i++;
	public static final String CTG_CIT_SECTION_NAME = "ctg_cit_section_name"; // INT
	public static final int CTG_CIT_SECTION_NAME_COL = i++;
	public static final String CTG_CIT_META_STATUS = "ctg_cit_meta_status"; // VARCHAR(256)
	public static final int CTG_CIT_META_STATUS_COL = i++;
	public static final String CTG_CIT_BY_USER = "ctg_cit_by_user"; // INT
	public static final int CTG_CIT_BY_USER_COL = i++;
	public static final String CTG_CIT_UPLOAD_DATE = "ctg_cit_upload_date"; // VARCHAR(256)
	public static final int CTG_CIT_UPLOAD_DATE_COL = i++;
	public static final String CTG_CIT_CLIENT_REF_INDEX = "ctg_cit_client_ref_index"; // INT
	public static final int CTG_CIT_CLIENT_REF_INDEX_COL = i++;
	public static final String CTG_CIT_CLIENT_INDEX = "ctg_cit_client_index"; // VARCHAR(64)
	public static final int CTG_CIT_CLIENT_INDEX_COL = i++;
	public static final String CTG_CIT_CLIENT_UUID = "ctg_cit_client_uuid"; // INTEGER NOT NULL DEFAULT 0
	public static final int CTG_CIT_CLIENT_UUID_COL = i++;
	public static final String CTG_CIT_LOCALLY_CHANGED = "ctg_cit_locally_changed"; // INTEGER NOT NULL DEFAULT 0
	public static final int CTG_CIT_LOCALLY_CHANGED_COL = i++;

	public static final int MAX_NAME_LENGTH = 256;
	
	//db migrations
//	public static final String CTG_TAG_LOCALLY_CHANGED_TYPE = "INTEGER NOT NULL DEFAULT 0";

	private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " ( " 
			+ CTG_CIT_ID + " 				INTEGER NOT NULL DEFAULT 0,"
			+ CTG_CIT_TEMPLATE_REF + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CIT_QUESTION + "    		TEXT," 
			+ CTG_CIT_TYPE + "    			TEXT NOT NULL DEFAULT '',"  
			+ CTG_CIT_EXTRA + "    			TEXT NOT NULL DEFAULT '',"  
			+ CTG_CIT_SECTION_ORDER + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CIT_SECTION_INDEX + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CIT_SECTION_NAME + "    	TEXT NOT NULL DEFAULT '',"  
			+ CTG_CIT_META_STATUS + "    	TEXT NOT NULL DEFAULT ''," 
			+ CTG_CIT_BY_USER + "    		INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CIT_UPLOAD_DATE + "    	TEXT NOT NULL DEFAULT ''," 
			+ CTG_CIT_CLIENT_REF_INDEX + "  INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CIT_CLIENT_INDEX + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CIT_CLIENT_UUID + "    	TEXT NOT NULL DEFAULT ''," 
			+ CTG_CIT_LOCALLY_CHANGED + "   INTEGER NOT NULL DEFAULT 0," 
			+ " PRIMARY KEY(" + CTG_CIT_ID + ", " + CTG_CIT_CLIENT_INDEX + ", " + CTG_CIT_CLIENT_UUID + " ) " + 
			")";

	// NOTE: When adding to the table items added locally: i.e. no id field, the
	// client needs to put in the client_uuid and client_index
	// Something like: INSERT INTO Log (id, rev_no, description) VALUES ((SELECT
	// IFNULL(MAX(id), 0)) + 1 FROM Log), 'rev_Id', 'some description')

	private static final String INDEX_1_NAME = DATABASE_TABLE+"_index_1";//NOTE: Indexes must be unique across all tables in db
	private static final String INDEX_1 = "CREATE INDEX " + INDEX_1_NAME
			+ " ON " + DATABASE_TABLE + " (  `" + CTG_CIT_SECTION_INDEX + "`, `"+ CTG_CIT_SECTION_ORDER + "`, `"
			+ CTG_CIT_TEMPLATE_REF + "`, `" + CTG_CIT_META_STATUS + "`, `"
			+ CTG_CIT_UPLOAD_DATE + "`, `" + CTG_CIT_ID + "`, `"
			+ CTG_CIT_LOCALLY_CHANGED + "` )";

	private static final String UPDATE_CLIENT_INDEX = "UPDATE "+DATABASE_TABLE+" SET "+CTG_CIT_CLIENT_INDEX+" = (SELECT (MAX("+CTG_CIT_CLIENT_INDEX+") + 1) FROM "+DATABASE_TABLE+" WHERE "+CTG_CIT_CLIENT_UUID+" = ?) WHERE ROWID=? ";

	private SQLiteDatabase db;
	// holds the app using the db
	private CTGTagTableDbHelper dbHelper;

//	protected int last_version = 0;

	public CTGChecklistItemTemplateTable(Context _context) {
		dbHelper = new CTGTagTableDbHelper(_context, DATABASE_NAME, null,
				DATABASE_VERSION); // DATABASE_VERSION);
	}

	/**
	 * This method should be called from the ConnectionManager which allows only
	 * a limited number of open database helper objects (ie. initially a single
	 * connection - all others wait)
	 * 
	 * @return result object with SQLiteDatabase in result.object if successful
	 */
	public synchronized Result openDb() {
		Result r = new Result();
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLException e) {
			// SQLiteDatabaseLockedException is only available starting in
			// Honeycomb - here we fake it
			if (e.getClass().getName()
					.contains("SQLiteDatabaseLockedException")) {
				// design decision here. Db should not be locked if I wait for
				// the previous activity to close
				// fully before opening the next one.
				r.error = "Database is locked. " + e.getMessage();
				r.success = false;
			} else {
				r.error = "Could not open database. " + e.getMessage();
				r.success = false;
			}
			Log.e(this.getClass().getName(), r.error);
		}
		return r;
	}

	@Override
	public void closeDb() {
		if (db != null)
			db.close();
	}
	
	public void dropTable() {
		db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
		//invalidate table
		invalidateTable();
	}

	@Override
	public void createTable() {
		dbHelper.onCreate(db);
	}

	@Override
	public void updateTable(int last_version) {
		dbHelper.onUpgrade(db, last_version, TABLE_VERSION);
	}

	@Override
	public String getTableName() {
		return DATABASE_TABLE;
	}

	@Override
	public int getTableCodeVersion() {
		return TABLE_VERSION;
	}
	//======================----------------> Db Access Methods <----------------================\\
	public void wipeTable() {
		synchronized (DATABASE_TABLE) {
			db.delete(DATABASE_TABLE, null, null);
		}
	}
	
	public void beginTransaction() {
		db.beginTransaction();
	}
	
	public void endTransaction() {
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public Result createLocal(CTGChecklistItemTemplate cit) {
		synchronized (DATABASE_TABLE) {
			int rowId = -1;
			Result r = new Result();
			try {
				//get latest value of clientIndex and add 1
				int nextIndex = getNextClientIndex(cit.getClientUUID());
				cit.setClientIndex(nextIndex);
				Assert.assertTrue("Could not get the next rci client index for uuid: "+cit.getClientUUID(), nextIndex > 0);
				//get sectionIndex the item is being inserted into - set if less than zero
				if (cit.getSectionOrder() < 1) {
					cit.setSectionOrder(getNextSectionOrder(cit));
				}
				//need to get latest sectionOrder for that index and add one,  all other items with a higher number need to be reordered
				r.add_results_if_error(reorderFromSectionOrder(cit), "Could not reorder checklist item templates.");
				ContentValues cv = getContentValues(cit);
				rowId = (int) db.insert(DATABASE_TABLE, null, cv);
				Assert.assertTrue("Could not insert a locally created run checklist item: "+cit.toString(), rowId > 0);
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			if (rowId < 1) {
				r.technical_error = "Could not add CTGChecklistItemTemplate in db. Data: "+cit.toString();
				r.success = false;
			}
			return r;
		}
	}
	
	public int getNextClientIndex(String uuid) {
		synchronized(this) {
			int i=1;
			Cursor c = db.rawQuery("SELECT MAX("+CTG_CIT_CLIENT_INDEX+") FROM "+ DATABASE_TABLE+" WHERE "+CTG_CIT_CLIENT_UUID+" = ? ", 
					new String[] { uuid});
			if (c.moveToFirst()){
				i = c.getInt(0)+1;
			}
			c.close();
			return i;
		}
	}
	
	
	public int getNextSectionOrder(CTGChecklistItemTemplate cit) {
		synchronized(this) {
			int i=1;
			Cursor c;
			//Note use of String.valueOf() which is faster than +"" and will work if the type changes
			if (cit.getTemplateRef() > 0) { //don't check client ref indexes - or we could miss items
				c = db.rawQuery("SELECT MAX("+CTG_CIT_SECTION_ORDER+") FROM "+ DATABASE_TABLE+" WHERE "+CTG_CIT_TEMPLATE_REF+" = ? "+
						" AND "+CTG_CIT_SECTION_INDEX+" <= ? ", 
					new String[] { String.valueOf(cit.getTemplateRef()), String.valueOf(cit.getSectionIndex()) });
			} else {
				c = db.rawQuery("SELECT MAX("+CTG_CIT_SECTION_ORDER+") FROM "+ DATABASE_TABLE+" WHERE "+CTG_CIT_CLIENT_REF_INDEX+" = ? "+
						" AND "+CTG_CIT_CLIENT_UUID+" = ? AND "+CTG_CIT_SECTION_INDEX+" <= ? ", 
					new String[] { String.valueOf(cit.getClientRefIndex()), 
							String.valueOf(cit.getClientUUID()), 
							String.valueOf(cit.getSectionIndex()) });
			}
			if (c.moveToFirst()){
				i = c.getInt(0)+1;
			}
			c.close();
			return i;
		}
	}
	
	/**
	 * This method takes the given item and inspects the current sectionOrder number.
	 * Any items belonging to the same collection (ie. run checklist, or template, etc) that match the sectionOrder have
	 * their value incremented by one.  This is to allow the inclusion or reordering of an item.
	 * 
	 * This function increases the number to allow for insertion of a new number, but does not ensure that the sectionOrder
	 * numbers generated are unique.  It is expected they are already defined as unique.
	 * @param cit
	 * @return
	 */
 	public Result reorderFromSectionOrder(CTGChecklistItemTemplate cit) {
		synchronized (DATABASE_TABLE) {
			Result r = new Result();
			try {
				ContentValues cv = getContentValues(cit);
				//for server created linked run checklists
				//UPDATE ctg_run_checklist_item_table SET ctg_rci_section_order = ctg_rci_section_order +1 WHERE ctg_rci_run_checklist_ref = 0 AND ctg_rci_section_order >= 0
				if (cit.getTemplateRef() > 0) {
					db.rawQuery("UPDATE "+DATABASE_TABLE+" SET "+CTG_CIT_SECTION_ORDER+"="+CTG_CIT_SECTION_ORDER+" +1 WHERE "+CTG_CIT_TEMPLATE_REF+" = ? AND "+CTG_CIT_SECTION_ORDER+" >= ?", 
						new String[] {String.valueOf(cit.getTemplateRef()), String.valueOf(cit.getSectionOrder())});
				} else { //reordering items belonging to a locally created group
					db.rawQuery("UPDATE "+DATABASE_TABLE+" SET "+CTG_CIT_SECTION_ORDER+"="+CTG_CIT_SECTION_ORDER+" +1 WHERE "+
							CTG_CIT_CLIENT_REF_INDEX+" = ? AND "+
							CTG_CIT_CLIENT_UUID+" = ? AND "+CTG_CIT_SECTION_ORDER+" >= ?", 
							new String[] {String.valueOf(cit.getClientRefIndex()), 
							String.valueOf(cit.getClientUUID()),
							String.valueOf(cit.getSectionOrder())});
				}
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			return r;
		}
	}
	 
	 
	/**
	 * Used to insert data into to local database
	 * @param c
	 * @return
	 */
	public Result insertOrUpdate(CTGChecklistItemTemplate t) {
		synchronized (DATABASE_TABLE) {
			int index = -1;
			Result r = new Result();
			try {
				ContentValues cv = getContentValues(t);
				index = (int) db.replace(DATABASE_TABLE, null, cv);
				r.last_insert_id = index;
				//if id > 0 && client_index > 0 - we need to check if we need to delete a locally created object
				if (t.getId() > 0 && t.getClientIndex() > 0) {
					//delete any locally created objects matching id=0, client_index=x, client_uuid=y
					removeLocallyCreatedItem(t.getClientRefIndex(), t.getClientIndex(), t.getClientUUID());
//					db.delete(DATABASE_TABLE, CTG_TAG_ID+" = 0 AND "+CTG_TAG_CLIENT_INDEX+" = ? AND "+CTG_TAG_CLIENT_UUID+" = ?", new String[] {t.getClient_index()+"", t.getClient_uuid()});
				}
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			return r;
		}
	}

	public int removeLocallyCreatedItem(int clientRefIndex, int clientIndex, String clientUUID) {
		return db.delete(DATABASE_TABLE, CTG_CIT_ID+" = 0 AND "+CTG_CIT_CLIENT_REF_INDEX+" = ? AND "+CTG_CIT_CLIENT_INDEX+" = ? AND "+CTG_CIT_CLIENT_UUID+" = ?", 
				new String[] {clientRefIndex+"", clientIndex+"", clientUUID});
	}
	
	private SQLiteStatement bindToReplace(SQLiteStatement stmt, CTGChecklistItemTemplate ct) {
		stmt.clearBindings();
		stmt.bindLong(CTG_CIT_ID_COL+1, ct.getId());
		stmt.bindLong(CTG_CIT_TEMPLATE_REF_COL+1, ct.getTemplateRef());
		stmt.bindString(CTG_CIT_QUESTION_COL+1, ct.getQuestion());
		stmt.bindString(CTG_CIT_TYPE_COL+1, ct.getType());
		stmt.bindString(CTG_CIT_EXTRA_COL+1, ct.getExtra());
		stmt.bindLong(CTG_CIT_SECTION_ORDER_COL+1, ct.getSectionOrder());
		stmt.bindLong(CTG_CIT_SECTION_INDEX_COL+1, ct.getSectionIndex());
		stmt.bindString(CTG_CIT_SECTION_NAME_COL+1, ct.getSectionName());
		stmt.bindLong(CTG_CIT_META_STATUS_COL+1, ct.getMeta_status());
		stmt.bindLong(CTG_CIT_BY_USER_COL+1, ct.getByUser());
		stmt.bindString(CTG_CIT_UPLOAD_DATE_COL+1, ct.getUploadDatetime());
		stmt.bindLong(CTG_CIT_CLIENT_REF_INDEX_COL+1, ct.getClientRefIndex());
		stmt.bindLong(CTG_CIT_CLIENT_INDEX_COL+1, ct.getClientIndex());
		stmt.bindString(CTG_CIT_CLIENT_UUID_COL+1, ct.getClientUUID());
		stmt.bindLong(CTG_CIT_LOCALLY_CHANGED_COL+1, ct.getLocally_changed());
		return stmt;
	}

	
	public Result insertOrUpdateArrayList(ArrayList<CTGChecklistItemTemplate> list) {
		Result r = new Result();
		String sql = "INSERT OR REPLACE INTO "+DATABASE_TABLE+" VALUES(?,?,?,?,?,  ?,?,?,?,?, ?,?,?,?,?) "; //15 items
		SQLiteStatement statement = db.compileStatement(sql);
		beginTransaction();
		for (CTGChecklistItemTemplate item : list) {
			bindToReplace(statement, item);
			try {
				statement.execute();	
				if (item.getId() > 0 && item.getClientIndex() > 0) {
					removeLocallyCreatedItem(item.getClientRefIndex(), item.getClientIndex(), item.getClientUUID());
				}
			} catch(SQLException e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
		}
		endTransaction();
		statement.close();
		return r;
	}
	
	
	public ArrayList<CTGChecklistItemTemplate> getValidTemplateItems(int templateRef, int clientRefIndex, String uuid) {
		ArrayList<CTGChecklistItemTemplate> al = new ArrayList<CTGChecklistItemTemplate>();
		Cursor list = getValidTemplateItemsCursor(templateRef, clientRefIndex, uuid);
		
		//these lines below were removed because when the database back-end is updated
		//the display still holds the reference without the new server id
		//so look for both items created only locally and those from the server too

//		if (templateRef > 0) //get template that was downloaded from the server
//			list = getValidTemplateItemsCursor(templateRef);
//		else //get locally created template
//			list = getValidTemplateItemsCursor(clientRefIndex, uuid);
		if (list.moveToFirst()){
			do {
				al.add(getFromCursor(list));
			} while(list.moveToNext());
		}
		list.close();
		return al;
	}
	
//	/**
//	 * Returns the cursor objects.
//	 * @return ArrayList<CTGChecklistItemTemplate>
//	 */
//	public Cursor getValidTemplateItemsCursor(int templateRef) {
//		return db.query(DATABASE_TABLE, null, 
//				CTG_CIT_TEMPLATE_REF+" = "+templateRef+" AND "+CTG_CIT_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL,
//				null, null, null, CTG_CIT_SECTION_INDEX+", "+CTG_CIT_SECTION_ORDER);
//	}
	
	/**
	 * Returns the cursor objects.
	 * @return ArrayList<CTGChecklistItemTemplate>
	 */
	public Cursor getValidTemplateItemsCursor(int templateRef, int clientRefIndex, String uuid) {
		return db.query(DATABASE_TABLE, null, 
				"("+CTG_CIT_TEMPLATE_REF+" = ? AND "+CTG_CIT_TEMPLATE_REF+" > 0 OR ( "+
						CTG_CIT_CLIENT_REF_INDEX+" = ? AND "+CTG_CIT_CLIENT_UUID+" = ? AND "+CTG_CIT_CLIENT_REF_INDEX+" > 0) ) AND "+
				CTG_CIT_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL,
				new String[] { templateRef+"", clientRefIndex+"", uuid}, null, null, CTG_CIT_SECTION_INDEX+", "+CTG_CIT_SECTION_ORDER);
	}
	
	/**
	 * This function will return a cursor for the items that will need to be updated when moving the position of a CIT object.
	 * NOTE: This will include the item being moved along with the other items to be reordered.
	 * 
	 * Worst case is when one item was created locally and hasn't been given a server id yet, and the other one has a server id
	 * @param from
	 * @param to
	 * @return
	 */
	public Cursor getItemsToReorderCursor(CTGChecklistItemTemplate from, CTGChecklistItemTemplate to) {
		//get best matching RC id information
		int ct_id=to.getTemplateRef();
		int client_ct_index=to.getClientRefIndex();
		String uuid=to.getClientUUID();
		//Restore original settings if the to item did not set it.
		if (ct_id < 1) ct_id = from.getTemplateRef();
		if (client_ct_index < 1) client_ct_index = from.getClientRefIndex();
		if (uuid.length() < 1) uuid = from.getClientUUID();
		
		String where = CTG_CIT_SECTION_ORDER+" >= ? AND ( ("+CTG_CIT_TEMPLATE_REF+" = ? AND "+CTG_CIT_TEMPLATE_REF+" > 0) OR "+
				"("+CTG_CIT_CLIENT_REF_INDEX+" = ? AND "+CTG_CIT_CLIENT_UUID+" = ? AND "+CTG_CIT_CLIENT_REF_INDEX+" > 0) )";
		Utils.LogD(this.getClass().getName(), "getItemsToReorderCursor(..) called - to_section_order:"+to.getSectionOrder()+", rc ref:"+ct_id+", client rc ref index:"+client_ct_index+", where sql: "+where);
		//for server created linked run checklists
		//or items belonging to a locally created group
		return db.query(DATABASE_TABLE, null, where,
				new String[] {to.getSectionOrder()+"", ct_id+"", 
				client_ct_index+"", uuid}, null, null, CTG_CIT_SECTION_ORDER);
	}
	
	/**
	 * 	
	 * This function will gather the items that will need to be updated when moving the position of an CIT object.
	 * NOTE: This will NOT include the item being moved, only the other items to be reordered.
	 * @param from
	 * @param to
	 * @return
	 */
	public ArrayList<CTGChecklistItemTemplate> getOtherItemsToReorder(CTGChecklistItemTemplate from, CTGChecklistItemTemplate to) {
		ArrayList<CTGChecklistItemTemplate> al = new ArrayList<CTGChecklistItemTemplate>();
		Cursor list = getItemsToReorderCursor(from, to);
		if (list.moveToFirst()){
			do {
				CTGChecklistItemTemplate item = getFromCursor(list);
				if (item.hasMatchingId(from)) //ignore it, this is the one being moved and will be handled after the items are reordered
					continue;
				al.add(item);
			} while(list.moveToNext());
		}
		list.close();
		Utils.LogD(this.getClass().getName(), "Items to be reordered: "+al.size());
		return al;
	}
	
	public ArrayList<CTGChecklistItemTemplate> getUploadItems() {
		ArrayList<CTGChecklistItemTemplate> al = new ArrayList<CTGChecklistItemTemplate>();
		Cursor list = getUploadItemsCursor();
		if (list.moveToFirst()){
			do {
				al.add(getFromCursor(list));
			} while(list.moveToNext());
		}
		list.close();
		return al;
	}
	
	/**
	 * Returns the cursor objects.
	 * @return cursor to get ArrayList<CTGChecklistItemTemplate>
	 */
	public Cursor getUploadItemsCursor() {
		return db.query(DATABASE_TABLE, null, 
				CTG_CIT_LOCALLY_CHANGED+" > 0",
				null, null, null, null);
	}
	
	public CTGChecklistItemTemplate getEntry(int id) {
		//String where = KEY_NUM+" = "+user_num;
		CTGChecklistItemTemplate c= new CTGChecklistItemTemplate();
		Cursor result= db.query(DATABASE_TABLE, 
				null, 
				CTG_CIT_ID+" = ? ", new String[] {id+""}, null, null, null);
		if (result.moveToFirst() ) { //make sure data is in the result.  Read only first entry
			c = getFromCursor(result);
		}
		result.close();
		return c;
	}
	
	/**
	 * Retrieve all entries for testing purposes
	 * @return ArrayList<CTGChecklistItemTemplate>
	 */
	public ArrayList<CTGChecklistItemTemplate> getAllEntries() {
		ArrayList<CTGChecklistItemTemplate> al = new ArrayList<CTGChecklistItemTemplate>();
		Cursor list = getAllEntriesCursor();
		if (list.moveToFirst()){
			do {
				al.add(getFromCursor(list));
			} while(list.moveToNext());
		}
		list.close();
		return al;
	}
	/**
	 * Used for testing
	 * @return
	 */
	protected Cursor getAllEntriesCursor() {
		return db.query(DATABASE_TABLE, null, null, null, null, null, CTG_CIT_ID);
	}
	
	
	// ================------------> helpers <-----------==============\\
    /** returns a ContentValues object for database insertion
     * 
     * @return
     */
    public ContentValues getContentValues(CTGChecklistItemTemplate c) {
    	ContentValues vals = new ContentValues();
    	//prepare info for db insert/update
    	vals.put(CTG_CIT_ID, c.getId());
    	vals.put(CTG_CIT_TEMPLATE_REF, c.getTemplateRef());
    	vals.put(CTG_CIT_QUESTION, c.getQuestion());
    	vals.put(CTG_CIT_TYPE, c.getType());
    	vals.put(CTG_CIT_EXTRA, c.getExtra());
    	vals.put(CTG_CIT_SECTION_ORDER, c.getSectionOrder());
    	vals.put(CTG_CIT_SECTION_INDEX, c.getSectionIndex());
    	vals.put(CTG_CIT_SECTION_NAME, c.getSectionName());
    	vals.put(CTG_CIT_META_STATUS, c.getMeta_status());
    	vals.put(CTG_CIT_BY_USER, c.getByUser());
    	vals.put(CTG_CIT_UPLOAD_DATE, c.getUploadDatetime());
    	vals.put(CTG_CIT_CLIENT_REF_INDEX, c.getClientRefIndex());
    	vals.put(CTG_CIT_CLIENT_INDEX, c.getClientIndex());
    	vals.put(CTG_CIT_CLIENT_UUID, c.getClientUUID());
    	vals.put(CTG_CIT_LOCALLY_CHANGED, c.getLocally_changed());
		return vals;
    }

    	
	/**
	 * Get the data for the item currently pointed at by the database
	 * @param record
	 * @return
	 */
	public CTGChecklistItemTemplate getFromCursor(Cursor record) {
		CTGChecklistItemTemplate c= new CTGChecklistItemTemplate();
		
		if (record.getColumnCount() > 1){ //make sure data is in the result.  Read only first entry
	    	c.setId(record.getInt(CTG_CIT_ID_COL));
			c.setTemplateRef(record.getInt(CTG_CIT_TEMPLATE_REF_COL));
			c.setQuestion(record.getString(CTG_CIT_QUESTION_COL));
			c.setType(record.getString(CTG_CIT_TYPE_COL));
			c.setExtra(record.getString(CTG_CIT_EXTRA_COL));
			c.setSectionOrder(record.getInt(CTG_CIT_SECTION_ORDER_COL));
			c.setSectionIndex(record.getInt(CTG_CIT_SECTION_INDEX_COL));
			c.setSectionName(record.getString(CTG_CIT_SECTION_NAME_COL));
			c.setMeta_status(record.getInt(CTG_CIT_META_STATUS_COL));
			c.setByUser(record.getInt(CTG_CIT_BY_USER_COL));
			c.setUploadDatetime(record.getString(CTG_CIT_UPLOAD_DATE_COL));
			c.setClientRefIndex(record.getInt(CTG_CIT_CLIENT_REF_INDEX_COL));
			c.setClientIndex(record.getInt(CTG_CIT_CLIENT_INDEX_COL));
			c.setClientUUID(record.getString(CTG_CIT_CLIENT_UUID_COL));
			c.setLocally_changed(record.getInt(CTG_CIT_LOCALLY_CHANGED_COL));
		}
		return c;
	}

    // ================------------> helper class <-----------==============\\
	private static class CTGTagTableDbHelper extends SQLiteOpenHelper {

		public CTGTagTableDbHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(getClass().getName(), "Table: "+DATABASE_TABLE+" was not found in db path: "+db.getPath()+"... creating table.");
			db.execSQL(DATABASE_CREATE);
			db.execSQL(INDEX_1);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// called when the version of the existing db is less than the
			// current
			Log.w(this.getClass().getName(), "Upgrading table from " + oldVersion + " to " + newVersion);
//			if (oldVersion < 1) { // if old version was V1, just add field
//				// create new table
//				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
//				onCreate(db);
//				Log.d(this.getClass().getName(), "Creating new "+ DATABASE_TABLE + " Table");
//			}
//			if (oldVersion < 3) {
//				//add locally changed field
//				db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD COLUMN "+NEW_COLUMN+" "+NEW_COLUMN_TYPE);
//				db.execSQL("DROP INDEX IF EXISTS "+INDEX_1_NAME);
//				db.execSQL(INDEX_1);
//				Log.d(this.getClass().getName(), "Adding new field and new index to "	+ DATABASE_TABLE + " Table");
//			}
		}

	}



}
