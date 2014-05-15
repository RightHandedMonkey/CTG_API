package com.worxforus.ctg.db;

import java.sql.SQLData;
import java.util.ArrayList;

import junit.framework.Assert;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import com.worxforus.Result;
import com.worxforus.ctg.CTGRunChecklist;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.ctg.CTGRunChecklistItem;
import com.worxforus.db.TableInterface;

public class CTGRunChecklistTable extends TableInterface<CTGRunChecklist> {

	public static final String DATABASE_NAME = CTGConstants.DATABASE_NAME;
	public static final String DATABASE_TABLE = "ctg_run_checklist_table";
	public static final int TABLE_VERSION = 1;
	// 1 - Initial version

	static int i = 0; // counter for field index
	public static final String CTG_RC_ID = "ctg_rc_id"; // int
	public static final int CTG_RC_ID_COL = i++;
	public static final String CTG_RC_TITLE = "ctg_rc_title"; // TEXT  DEFAULT ''
	public static final int CTG_RC_TITLE_COL = i++;
	public static final String CTG_RC_TEMPLATE_REF = "ctg_rc_template_ref"; // INT
	public static final int CTG_RC_TEMPLATE_REF_COL = i++;
	public static final String CTG_RC_META_STATUS = "ctg_rc_meta_status"; // int
	public static final int CTG_RC_META_STATUS_COL = i++;
	public static final String CTG_RC_BY_USER = "ctg_rc_by_user"; // int
	public static final int CTG_RC_BY_USER_COL = i++;
	public static final String CTG_RC_UPLOAD_DATE = "ctg_rc_upload_date"; // TEXT
	public static final int CTG_RC_UPLOAD_DATE_COL = i++;
	public static final String CTG_RC_NUM_ITEMS = "ctg_rc_num_items"; // INT
	public static final int CTG_RC_NUM_ITEMS_COL = i++;
	public static final String CTG_RC_NUM_COMPLETE = "ctg_rc_num_complete"; // INT
	public static final int CTG_RC_NUM_COMPLETE_COL = i++;
	public static final String CTG_RC_CLIENT_REF_INDEX = "ctg_rc_client_ref_index"; // INT
	public static final int CTG_RC_CLIENT_REF_INDEX_COL = i++;
	public static final String CTG_RC_CLIENT_INDEX = "ctg_rc_client_index"; // INT
	public static final int CTG_RC_CLIENT_INDEX_COL = i++;
	public static final String CTG_RC_CLIENT_UUID = "ctg_rc_client_uuid"; // TEXT
	public static final int CTG_RC_CLIENT_UUID_COL = i++;
	public static final String CTG_RC_LOCALLY_CHANGED = "ctg_rc_locally_changed"; // INTEGER NOT NULL DEFAULT 0
	public static final int CTG_RC_LOCALLY_CHANGED_COL = i++;

	public static final int MAX_NAME_LENGTH = 256;
	
	public static final String TEMP_RC_NAME = "My Checklist";
	
	//db migrations
//	public static final String CTG_TAG_LOCALLY_CHANGED_TYPE = "INTEGER NOT NULL DEFAULT 0";

	private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " ( " 
			+ CTG_RC_ID + " 			INTEGER NOT NULL DEFAULT 0,"
			+ CTG_RC_TITLE + "    		TEXT NOT NULL DEFAULT ''," 
			+ CTG_RC_TEMPLATE_REF + "   INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_META_STATUS + "    INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_BY_USER + "    	INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_UPLOAD_DATE + "    TEXT," 
			+ CTG_RC_NUM_ITEMS + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_NUM_COMPLETE + "   INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_CLIENT_REF_INDEX+" INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_CLIENT_INDEX + "   INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_CLIENT_UUID + "    TEXT NOT NULL DEFAULT ''," 
			+ CTG_RC_LOCALLY_CHANGED +" INTEGER NOT NULL DEFAULT 0," 
			+ " PRIMARY KEY(" + CTG_RC_ID + ", " + CTG_RC_CLIENT_INDEX + ", " + CTG_RC_CLIENT_UUID + " ) " + 
			")";

	// NOTE: When adding to the table items added locally: i.e. no id field, the
	// client needs to put in the client_uuid and client_index
	// Something like: INSERT INTO Log (id, rev_no, description) VALUES ((SELECT
	// IFNULL(MAX(id), 0)) + 1 FROM Log), 'rev_Id', 'some description')

	private static final String INDEX_1_NAME = DATABASE_TABLE+"_index_1";//NOTE: Indexes must be unique across all tables in db
	private static final String INDEX_1 = "CREATE INDEX " + INDEX_1_NAME
			+ " ON " + DATABASE_TABLE + " (  `"+CTG_RC_TITLE+"`, `" + CTG_RC_CLIENT_INDEX + "`, `"+ CTG_RC_CLIENT_UUID + "`, `"
			+ CTG_RC_TEMPLATE_REF + "`, `" + CTG_RC_META_STATUS + "`, `"
			+ CTG_RC_UPLOAD_DATE + "`, `" + CTG_RC_ID + "`, `"
			+ CTG_RC_LOCALLY_CHANGED + "` )";

	private static final String UPDATE_CLIENT_INDEX = "UPDATE "+DATABASE_TABLE+" SET "+CTG_RC_CLIENT_INDEX+" = (SELECT (MAX("+CTG_RC_CLIENT_INDEX+") + 1) FROM "+DATABASE_TABLE+" WHERE "+CTG_RC_CLIENT_UUID+" = ? ) WHERE ROWID=? ";
	
	private SQLiteDatabase db;
	// holds the app using the db
	private CTGTagTableDbHelper dbHelper;

//	protected int last_version = 0;

	public CTGRunChecklistTable(Context _context) {
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
	
	/**
	 * Used to insert data into to local database
	 * @param c
	 * @return
	 */
	public Result insertOrUpdate(CTGRunChecklist item) {
		synchronized (DATABASE_TABLE) {
			int index = -1;
			Result r = new Result();
			try {
				ContentValues cv = getContentValues(item);
				index = (int) db.replace(DATABASE_TABLE, null, cv);
				r.last_insert_id = index;
				//if id > 0 && client_index > 0 - we need to check if we need to delete a locally created object
				if (item.getId() > 0 && item.getClientIndex() > 0) {
					//delete any locally created objects matching id=0, client_index=x, client_uuid=y
					removeLocallyCreatedItem(item.getClientRefIndex(), item.getClientIndex(), item.getClientUUID());
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
	 * Used to update data into the database.  
	 * Note: this function will not work to update the RC_ID with a server id, 
	 * since that is the primary key used for the lookup. 
	 * Instead use the insertOrUpdate(...) function
	 * @param c
	 * @return
	 */
	public Result update(CTGRunChecklist t) {
		synchronized (DATABASE_TABLE) {
			Result r = new Result();
			try {
				ContentValues cv = getContentValues(t);
				db.update(DATABASE_TABLE, cv, 
						CTG_RC_ID+" = ? AND "+CTG_RC_CLIENT_INDEX+" = ? AND "+CTG_RC_CLIENT_UUID+" = ?", 
						new String[] {t.getId()+"", t.getClientIndex()+"", t.getClientUUID()});
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			return r;
		}
	}
	
	public int removeLocallyCreatedItem(int clientRefIndex, int clientIndex, String clientUUID) {
		return db.delete(DATABASE_TABLE, CTG_RC_ID+" = 0 AND "+CTG_RC_CLIENT_REF_INDEX+" = ? AND "+CTG_RC_CLIENT_INDEX+" = ? AND "+CTG_RC_CLIENT_UUID+" = ?", 
				new String[] {clientRefIndex+"", clientIndex+"", clientUUID});
	}
	

	public int getNumTimesTemplateUsed(int templateId, int templateRefId, String templateUUID) {
		Cursor list = getNumTimesTemplateUsedCursor(templateId, templateRefId, templateUUID);
		int size = list.getCount();
		list.close();
		return size;
	}

	public Result createLocalRunChecklist(CTGRunChecklist rc) {
		synchronized (DATABASE_TABLE) {
			int rowId = -1;
			Result r = new Result();
			try {
				//get latest value of clientIndex and add 1
				int nextIndex = getNextClientIndex(rc.getClientUUID());
				rc.setClientIndex(nextIndex);
				Assert.assertTrue("Could not get the next Run Checklist client index for uuid: "+rc.getClientUUID(), nextIndex > 0);

				ContentValues cv = getContentValues(rc);
				rowId = (int) db.insert(DATABASE_TABLE, null, cv);
				Assert.assertTrue("Could not insert a locally created run checklist: "+rc.toString(), rowId > 0);
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			if (rowId > 0) {
				//get the newly created database item using the hidden rowid field
				rc = getByRowid(rowId);
				r.object = rc;
			} else {
				r.technical_error = "Could not add CTGRunChecklist in db. Data: "+rc.toString();
				r.success = false;
			}
			return r;
		}
	}

	public int getNextClientIndex(String uuid) {
		synchronized(this) {
			int i=1;
			Cursor c = db.rawQuery("SELECT MAX("+CTG_RC_CLIENT_INDEX+") FROM "+ DATABASE_TABLE+" WHERE "+CTG_RC_CLIENT_UUID+" = ? ", 
					new String[] { uuid});
			if (c.moveToFirst()){
				i = c.getInt(0)+1;
			}
			c.close();
			return i;
		}
	}
	
	private SQLiteStatement bindToReplace(SQLiteStatement stmt, CTGRunChecklist cit) {
		stmt.clearBindings();
		stmt.bindLong(CTG_RC_ID_COL+1, cit.getId());
		stmt.bindString(CTG_RC_TITLE_COL+1, cit.getTitle());
		stmt.bindLong(CTG_RC_TEMPLATE_REF_COL+1, cit.getTemplateRef());
		stmt.bindLong(CTG_RC_META_STATUS_COL+1, cit.getMeta_status());
		stmt.bindLong(CTG_RC_BY_USER_COL+1, cit.getByUser());
		stmt.bindString(CTG_RC_UPLOAD_DATE_COL+1, cit.getUploadDatetime());
		stmt.bindLong(CTG_RC_NUM_ITEMS_COL+1, cit.getNumItems());
		stmt.bindLong(CTG_RC_NUM_COMPLETE_COL+1, cit.getNumComplete());
		stmt.bindLong(CTG_RC_CLIENT_REF_INDEX_COL+1, cit.getClientRefIndex());
		stmt.bindLong(CTG_RC_CLIENT_INDEX_COL+1, cit.getClientIndex());
		stmt.bindString(CTG_RC_CLIENT_UUID_COL+1, cit.getClientUUID());
		stmt.bindLong(CTG_RC_LOCALLY_CHANGED_COL+1, cit.getLocally_changed());
		return stmt;
	}
	
	public Result insertOrUpdateArrayList(ArrayList<CTGRunChecklist> list) {
		Result r = new Result();
		String sql = "INSERT OR REPLACE INTO "+DATABASE_TABLE+" VALUES(?,?,?,?,?,  ?,?,?,?,?, ?,?) "; //12 items
		SQLiteStatement statement = db.compileStatement(sql);
		beginTransaction();
		for (CTGRunChecklist item : list) {
			bindToReplace(statement, item);
			try {
				statement.execute();	
				if (item.getId() > 0 && item.getClientIndex() > 0) {
					//delete any locally created objects matching id=0, client_index=x, client_uuid=y
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

	
	public ArrayList<CTGRunChecklist> getValidItems() {
		ArrayList<CTGRunChecklist> al = new ArrayList<CTGRunChecklist>();
		Cursor list = getValidItemsCursor();
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
	 * @return ArrayList<CTGRunChecklist>
	 */
	public Cursor getValidItemsCursor() {
		return db.query(DATABASE_TABLE, null, 
				CTG_RC_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL,
				null, null, null, null);
	}
	
	public ArrayList<CTGRunChecklist> getUploadItems() {
		ArrayList<CTGRunChecklist> al = new ArrayList<CTGRunChecklist>();
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
	 * @return cursor to get ArrayList<CTGRunChecklist>
	 */
	public Cursor getUploadItemsCursor() {
		return db.query(DATABASE_TABLE, null, 
				CTG_RC_LOCALLY_CHANGED+" > 0",
				null, null, null, null);
	}
	
	/**
	 * Gets the specific item, whether it was created locally or originated from the server.
	 * @param id - greater than zero from server originated objects
	 * @param localIndex - used for locally created objects
	 * @param uuid - used for locally created objects
	 * @return The object requested from the database or a new one if not found
	 */
	public CTGRunChecklist getEntry(int id, int localIndex, String uuid) {
		CTGRunChecklist c= new CTGRunChecklist();
		Cursor result;
		if (id > 0)
			result = getEntryCursor(id);
		else
			result = getEntryCursor(localIndex, uuid);
		if (result.moveToFirst() ) { //make sure data is in the result.  Read only first entry
			c = getFromCursor(result);
		}
		result.close();
		return c;
	}
	
	/**
	 * Get the cursor that finds a specific server created item
	 * @return
	 */
	protected Cursor getEntryCursor(int id) {
		return db.query(DATABASE_TABLE, null, CTG_RC_ID+" = ? ", new String[] {id+""}, null, null, null);
	}
	
	/**
	 * Get the cursor that finds a specific locally created item
	 * @return
	 */
	protected Cursor getEntryCursor(int index, String uuid) {
		return db.query(DATABASE_TABLE, null, CTG_RC_CLIENT_INDEX+" = ? AND "+CTG_RC_CLIENT_UUID+" = ? ",
				new String[] {index+"", uuid}, null, null, null);
	}
	
	/**
	 * Retrieve all entries for testing purposes
	 * @return ArrayList<CTGRunChecklist>
	 */
	public ArrayList<CTGRunChecklist> getAllEntries() {
		ArrayList<CTGRunChecklist> al = new ArrayList<CTGRunChecklist>();
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
		return db.query(DATABASE_TABLE, null, null, null, null, null, CTG_RC_ID);
	}

	protected Cursor getNumTimesTemplateUsedCursor(int templateId, int templateRefId, String templateUUID) {
		String where = " ( ("+CTG_RC_TEMPLATE_REF+" = ? AND "+CTG_RC_TEMPLATE_REF+" > 0) OR "+
				"("+CTG_RC_CLIENT_REF_INDEX+" = ? AND "+CTG_RC_CLIENT_UUID+" = ? AND "+CTG_RC_CLIENT_REF_INDEX+" > 0) )"+
				" AND "+CTG_RC_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL;
		//String where = CTG_RC_TEMPLATE_REF+" = ? AND "+CTG_RC_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL;

		return db.query(DATABASE_TABLE, null, where, new String[] {templateId+"", templateRefId+"", templateUUID}, null, null, CTG_RC_ID);
	}
	
	/**
	 * To make testing easier.  Not to be used by the actual app.
	 * @param rowid
	 * @return
	 */
	public CTGRunChecklist getByRowidTest(int rowid) { return getByRowid(rowid); }

	protected CTGRunChecklist getByRowid(int rowid) {
		CTGRunChecklist c= new CTGRunChecklist();
		Cursor result= db.query(DATABASE_TABLE, 
				null, 
				"ROWID = ? ", new String[] {rowid+""}, null, null, null);
		if (result.moveToFirst() ) { //make sure data is in the result.  Read only first entry
			c = getFromCursor(result);
		}
		result.close();
		return c;
	}

	
	// ================------------> helpers <-----------==============\\
    /** returns a ContentValues object for database insertion
     * 
     * @return
     */
    public ContentValues getContentValues(CTGRunChecklist c) {
    	ContentValues vals = new ContentValues();
    	//prepare info for db insert/update
    	vals.put(CTG_RC_ID, c.getId());
    	vals.put(CTG_RC_TITLE, c.getTitle());
    	vals.put(CTG_RC_TEMPLATE_REF, c.getTemplateRef());
    	vals.put(CTG_RC_META_STATUS, c.getMeta_status());
    	vals.put(CTG_RC_BY_USER, c.getByUser());
    	vals.put(CTG_RC_UPLOAD_DATE, c.getUploadDatetime());
    	vals.put(CTG_RC_NUM_ITEMS, c.getNumItems());
    	vals.put(CTG_RC_NUM_COMPLETE, c.getNumComplete());
    	vals.put(CTG_RC_CLIENT_REF_INDEX, c.getClientRefIndex());
    	vals.put(CTG_RC_CLIENT_INDEX, c.getClientIndex());
    	vals.put(CTG_RC_CLIENT_UUID, c.getClientUUID());
    	vals.put(CTG_RC_LOCALLY_CHANGED, c.getLocally_changed());
		return vals;
    }


	/**
	 * Get the data for the item currently pointed at by the database
	 * @param record
	 * @return
	 */
	public CTGRunChecklist getFromCursor(Cursor record) {
		CTGRunChecklist c= new CTGRunChecklist();
		
		if (record.getColumnCount() > 1){ //make sure data is in the result.  Read only first entry
	    	c.setId(record.getInt(CTG_RC_ID_COL));
			c.setTitle(record.getString(CTG_RC_TITLE_COL));
			c.setTemplateRef(record.getInt(CTG_RC_TEMPLATE_REF_COL));
			c.setMeta_status(record.getInt(CTG_RC_META_STATUS_COL));
			c.setByUser(record.getInt(CTG_RC_BY_USER_COL));
			c.setUploadDatetime(record.getString(CTG_RC_UPLOAD_DATE_COL));
			c.setNumItems(record.getInt(CTG_RC_NUM_ITEMS_COL));
			c.setNumComplete(record.getInt(CTG_RC_NUM_COMPLETE_COL));
			c.setClientRefIndex(record.getInt(CTG_RC_CLIENT_REF_INDEX_COL));
			c.setClientIndex(record.getInt(CTG_RC_CLIENT_INDEX_COL));
			c.setClientUUID(record.getString(CTG_RC_CLIENT_UUID_COL));
			c.setLocally_changed(record.getInt(CTG_RC_LOCALLY_CHANGED_COL));
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
