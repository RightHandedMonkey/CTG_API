package com.worxforus.ctg.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import com.worxforus.Result;
import com.worxforus.ctg.CTGRunChecklist;
import com.worxforus.ctg.CTGConstants;
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
			+ " PRIMARY KEY(" + CTG_RC_ID + ", "+ CTG_RC_CLIENT_REF_INDEX + ", " 
			+ CTG_RC_CLIENT_INDEX + ", " + CTG_RC_CLIENT_UUID + " ) " + 
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
	public Result insertOrUpdate(CTGRunChecklist t) {
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
		return db.delete(DATABASE_TABLE, CTG_RC_ID+" = 0 AND "+CTG_RC_CLIENT_REF_INDEX+" = ? AND "+CTG_RC_CLIENT_INDEX+" = ? AND "+CTG_RC_CLIENT_UUID+" = ?", 
				new String[] {clientRefIndex+"", clientIndex+"", clientUUID});
	}
	
	public Result insertOrUpdateArrayList(ArrayList<CTGRunChecklist> t) {
		Result r = new Result();
		beginTransaction();
		for (CTGRunChecklist ctgTag : t) {
			r.add_results_if_error(insertOrUpdate(ctgTag), "Could not add CTGRunChecklist "+t+" to database." );
		}
		endTransaction();
		return r;
	}
	
	/*
	 * 			+ CTG_RC_ID + " 			INTEGER NOT NULL DEFAULT 0,"
			+ CTG_RC_TITLE + "    		TEXT NOT NULL DEFAULT ''," 
			+ CTG_RC_TEMPLATE_REF + "   INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_META_STATUS + "    INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_BY_USER + "    	INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_UPLOAD_DATE + "    TEXT," 
			+ CTG_RC_NUM_ITEMS + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_NUM_COMPLETE + "   INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_CLIENT_REF_INDEX +"INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_CLIENT_INDEX + "   INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_CLIENT_UUID + "    TEXT NOT NULL DEFAULT ''," 
			+ CTG_RC_LOCALLY_CHANGED + "INTEGER NOT NULL DEFAULT 0," 
	 */
	public ArrayList<CTGRunChecklist> getValidItems(int templateRef) {
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
	 * @return ArrayList<CTGRunChecklist>
	 */
	public Cursor getUploadItemsCursor() {
		return db.query(DATABASE_TABLE, null, 
				CTG_RC_LOCALLY_CHANGED+" > 0",
				null, null, null, null);
	}
	
	public CTGRunChecklist getEntry(int id) {
		//String where = KEY_NUM+" = "+user_num;
		CTGRunChecklist c= new CTGRunChecklist();
		Cursor result= db.query(DATABASE_TABLE, 
				null, 
				CTG_RC_ID+" = ? ", new String[] {id+""}, null, null, null);
		if (result.moveToFirst() ) { //make sure data is in the result.  Read only first entry
			c = getFromCursor(result);
		}
		result.close();
		return c;
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

    	/*
			+ CTG_RC_ID + " 			INTEGER NOT NULL DEFAULT 0,"
			+ CTG_RC_TITLE + "    		TEXT NOT NULL DEFAULT ''," 
			+ CTG_RC_TEMPLATE_REF + "   INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_META_STATUS + "    INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_BY_USER + "    	INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_UPLOAD_DATE + "    TEXT," 
			+ CTG_RC_NUM_ITEMS + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_NUM_COMPLETE + "   INTEGER NOT NULL DEFAULT 0,"  
			+ CTG_RC_CLIENT_REF_INDEX +"INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_CLIENT_INDEX + "   INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RC_CLIENT_UUID + "    TEXT NOT NULL DEFAULT ''," 
			+ CTG_RC_LOCALLY_CHANGED + "INTEGER NOT NULL DEFAULT 0," 
    	 */
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
