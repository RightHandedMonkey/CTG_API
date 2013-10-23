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
import com.worxforus.ctg.CTGRunChecklistItem;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.db.TableInterface;

public class CTGRunChecklistItemTable extends TableInterface<CTGRunChecklistItem> {

	public static final String DATABASE_NAME = CTGConstants.DATABASE_NAME;
	public static final String DATABASE_TABLE = "ctg_run_checklist_item_table";
	public static final int TABLE_VERSION = 1;
	// 1 - Initial version

	static int i = 0; // counter for field index
	public static final String CTG_RCI_ID = "ctg_rci_id"; //INT
	public static final int CTG_RCI_ID_COL = i++;
	public static final String CTG_RCI_RUN_CHECKLIST_REF = "ctg_rci_run_checklist_ref"; //INT
	public static final int CTG_RCI_RUN_CHECKLIST_REF_COL = i++;
	public static final String CTG_RCI_CHECKLIST_ITEM_TEMPLATE_REF = "ctg_rci_checklist_item_template_ref";//INT
	public static final int CTG_RCI_CHECKLIST_ITEM_TEMPLATE_REF_COL = i++;
	public static final String CTG_RCI_QUESTION = "ctg_rci_question"; // TEXT
	public static final int CTG_RCI_QUESTION_COL = i++;
	public static final String CTG_RCI_TYPE = "ctg_rci_type"; // TEXT
	public static final int CTG_RCI_TYPE_COL = i++;
	public static final String CTG_RCI_EXTRA = "ctg_rci_extra"; // TEXT
	public static final int CTG_RCI_EXTRA_COL = i++;
	public static final String CTG_RCI_SECTION_ORDER = "ctg_rci_section_order"; // INT
	public static final int CTG_RCI_SECTION_ORDER_COL = i++;
	public static final String CTG_RCI_SECTION_INDEX = "ctg_rci_section_index"; // INT
	public static final int CTG_RCI_SECTION_INDEX_COL = i++;
	public static final String CTG_RCI_SECTION_NAME = "ctg_rci_section_name"; // TEXT
	public static final int CTG_RCI_SECTION_NAME_COL = i++;
	public static final String CTG_RCI_META_STATUS = "ctg_rci_meta_status"; // INT
	public static final int CTG_RCI_META_STATUS_COL = i++;
	public static final String CTG_RCI_BY_USER = "ctg_rci_by_user"; // INT
	public static final int CTG_RCI_BY_USER_COL = i++;
	public static final String CTG_RCI_UPLOAD_DATE = "ctg_rci_upload_date"; // TEXT
	public static final int CTG_RCI_UPLOAD_DATE_COL = i++;
	public static final String CTG_RCI_VALUE = "ctg_rci_value"; // TEXT
	public static final int CTG_RCI_VALUE_COL = i++;
	public static final String CTG_RCI_COMMENT = "ctg_rci_comment"; // TEXT
	public static final int CTG_RCI_COMMENT_COL = i++;
	public static final String CTG_RCI_CLIENT_RC_REF_INDEX = "ctg_rci_client_rc_ref_index"; // INT
	public static final int CTG_RCI_CLIENT_RC_REF_INDEX_COL = i++;
	public static final String CTG_RCI_CLIENT_CIT_REF_INDEX = "ctg_rci_client_cit_ref_index"; // INT
	public static final int CTG_RCI_CLIENT_CIT_REF_INDEX_COL = i++;
	public static final String CTG_RCI_CLIENT_INDEX = "ctg_rci_client_index"; // INT
	public static final int CTG_RCI_CLIENT_INDEX_COL = i++;
	public static final String CTG_RCI_CLIENT_UUID = "ctg_rci_client_uuid"; // TEXT
	public static final int CTG_RCI_CLIENT_UUID_COL = i++;
	public static final String CTG_RCI_LOCALLY_CHANGED = "ctg_rci_locally_changed"; // INT
	public static final int CTG_RCI_LOCALLY_CHANGED_COL = i++;

	public static final int DEFAULT_LENGTH = 256;
	public static final int VALUE_LENGTH = 32;
	public static final int TYPE_LENGTH = 32;
	public static final int EXTRA_LENGTH = 32;
	public static final int QUESTION_MAX_INPUT_LENGTH = 2048;
	

	private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " ( " 
			+ CTG_RCI_ID + 					" INTEGER NOT NULL DEFAULT 0,"
			+ CTG_RCI_RUN_CHECKLIST_REF + " INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RCI_CHECKLIST_ITEM_TEMPLATE_REF + " INTEGER NOT NULL DEFAULT 0, "
			+ CTG_RCI_QUESTION + " TEXT DEFAULT '',"  
			+ CTG_RCI_TYPE + " TEXT DEFAULT '',"  
			+ CTG_RCI_EXTRA + " TEXT DEFAULT '',"  
			+ CTG_RCI_SECTION_ORDER + " INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RCI_SECTION_INDEX + " INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RCI_SECTION_NAME + " TEXT NOT NULL DEFAULT ''," 
			+ CTG_RCI_META_STATUS + " INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RCI_BY_USER + " INTEGER NOT NULL DEFAULT 0," 
			+ CTG_RCI_UPLOAD_DATE + " TEXT NOT NULL DEFAULT ''," 
			+ CTG_RCI_VALUE + " TEXT DEFAULT ''," 
			+ CTG_RCI_COMMENT + " TEXT DEFAULT ''," 
			+ CTG_RCI_CLIENT_RC_REF_INDEX + " INTEGER NOT NULL DEFAULT 0,"
			+ CTG_RCI_CLIENT_CIT_REF_INDEX + " INTEGER NOT NULL DEFAULT 0,"
			+ CTG_RCI_CLIENT_INDEX + " INTEGER NOT NULL DEFAULT 0,"
			+ CTG_RCI_CLIENT_UUID + " TEXT NOT NULL DEFAULT ''," 
			+ CTG_RCI_LOCALLY_CHANGED + " INTEGER NOT NULL DEFAULT 0," 
			+ " PRIMARY KEY(" + CTG_RCI_ID + ", "+ CTG_RCI_CLIENT_RC_REF_INDEX + ", " 
			+ CTG_RCI_CLIENT_CIT_REF_INDEX + ", " + CTG_RCI_CLIENT_INDEX + ", " + CTG_RCI_CLIENT_UUID + " ) " + 
			")";


	// NOTE: When adding to the table items added locally: i.e. no id field, the
	// client needs to put in the client_uuid and client_index
	// Something like: INSERT INTO Log (id, rev_no, description) VALUES ((SELECT
	// IFNULL(MAX(id), 0)) + 1 FROM Log), 'rev_Id', 'some description')

	private static final String INDEX_1_NAME = DATABASE_TABLE+"_index_1";//NOTE: Indexes must be unique across all tables in db
	private static final String INDEX_1 = "CREATE INDEX " + INDEX_1_NAME
			+ " ON " + DATABASE_TABLE + " (  `" + CTG_RCI_SECTION_INDEX + "`, `"+ CTG_RCI_SECTION_ORDER + "`, `"
			+ CTG_RCI_RUN_CHECKLIST_REF+ "`, `"	+ CTG_RCI_CHECKLIST_ITEM_TEMPLATE_REF + "`, `" + CTG_RCI_META_STATUS + "`, `"
			+ CTG_RCI_UPLOAD_DATE + "`, `" + CTG_RCI_ID + "`, `"
			+ CTG_RCI_LOCALLY_CHANGED + "` )";

	private SQLiteDatabase db;
	// holds the app using the db
	private TableDbHelper dbHelper;

//	protected int last_version = 0;

	public CTGRunChecklistItemTable(Context _context) {
		dbHelper = new TableDbHelper(_context, DATABASE_NAME, null,
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
	public Result insertOrUpdate(CTGRunChecklistItem t) {
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
					removeLocallyCreatedItem(t.getClientRunChecklistRefIndex(), t.getClientChecklistItemTemplateRefIndex(), t.getClientIndex(), t.getClientUUID());
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
	 * Used to update data into the local database
	 * @param c
	 * @return
	 */
	public Result updateLocal(CTGRunChecklistItem t) {
		synchronized (DATABASE_TABLE) {
			Result r = new Result();
			try {
				ContentValues cv = getContentValues(t);
				db.update(DATABASE_TABLE, cv, 
						CTG_RCI_ID+" = ? AND "+CTG_RCI_CLIENT_RC_REF_INDEX+" = ? AND "+CTG_RCI_CLIENT_CIT_REF_INDEX+" = ? AND "+CTG_RCI_CLIENT_INDEX+" = ? AND "+CTG_RCI_CLIENT_UUID+" = ?", 
						new String[] {t.getId()+"", t.getClientRunChecklistRefIndex()+"", t.getClientChecklistItemTemplateRefIndex()+"", t.getClientIndex()+"", t.getClientUUID()});
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			return r;
		}
	}

	public int removeLocallyCreatedItem(int clientRCRefIndex, int clientCITRefIndex, int clientIndex, String clientUUID) {
		return db.delete(DATABASE_TABLE, CTG_RCI_ID+" = 0 AND "+CTG_RCI_CLIENT_RC_REF_INDEX+" = ? AND "+CTG_RCI_CLIENT_CIT_REF_INDEX+" = ? AND "+CTG_RCI_CLIENT_INDEX+" = ? AND "+CTG_RCI_CLIENT_UUID+" = ?", 
				new String[] {clientRCRefIndex+"", clientCITRefIndex+"", clientIndex+"", clientUUID});
	}
	
	public Result insertOrUpdateArrayList(ArrayList<CTGRunChecklistItem> t) {
		Result r = new Result();
		beginTransaction();
		for (CTGRunChecklistItem ctgTag : t) {
			r.add_results_if_error(insertOrUpdate(ctgTag), "Could not add CTGRunChecklistItem "+t+" to database." );
		}
		endTransaction();
		return r;
	}
	
	
	public ArrayList<CTGRunChecklistItem> getValidChecklistItems(int runChecklistRef) {
		ArrayList<CTGRunChecklistItem> al = new ArrayList<CTGRunChecklistItem>();
		Cursor list = getValidChecklistItemsCursor(runChecklistRef);
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
	 * @return ArrayList<CTGRunChecklistItem>
	 */
	public Cursor getValidChecklistItemsCursor(int runChecklistRef) {
		return db.query(DATABASE_TABLE, null, 
				CTG_RCI_RUN_CHECKLIST_REF+" = "+runChecklistRef+" AND "+CTG_RCI_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL,
				null, null, null, CTG_RCI_SECTION_INDEX+", "+CTG_RCI_SECTION_ORDER);
	}
	
	public ArrayList<CTGRunChecklistItem> getUploadItems() {
		ArrayList<CTGRunChecklistItem> al = new ArrayList<CTGRunChecklistItem>();
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
	 * @return ArrayList<CTGRunChecklistItem>
	 */
	public Cursor getUploadItemsCursor() {
		return db.query(DATABASE_TABLE, null, 
				CTG_RCI_LOCALLY_CHANGED+" > 0",
				null, null, null, null);
	}
	
	public CTGRunChecklistItem getEntry(int id) {
		//String where = KEY_NUM+" = "+user_num;
		CTGRunChecklistItem c= new CTGRunChecklistItem();
		Cursor result= db.query(DATABASE_TABLE, 
				null, 
				CTG_RCI_ID+" = ? ", new String[] {id+""}, null, null, null);
		if (result.moveToFirst() ) { //make sure data is in the result.  Read only first entry
			c = getFromCursor(result);
		}
		result.close();
		return c;
	}
	
	/**
	 * Retrieve all entries for testing purposes
	 * @return ArrayList<CTGRunChecklistItem>
	 */
	public ArrayList<CTGRunChecklistItem> getAllEntries() {
		ArrayList<CTGRunChecklistItem> al = new ArrayList<CTGRunChecklistItem>();
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
		return db.query(DATABASE_TABLE, null, null, null, null, null, CTG_RCI_ID);
	}
	
	
	// ================------------> helpers <-----------==============\\
    /** returns a ContentValues object for database insertion
     * 
     * @return
     */
    public ContentValues getContentValues(CTGRunChecklistItem c) {
    	ContentValues vals = new ContentValues();
    	//prepare info for db insert/update
    	vals.put(CTG_RCI_ID, c.getId());
    	vals.put(CTG_RCI_RUN_CHECKLIST_REF, c.getRunChecklistRef());
    	vals.put(CTG_RCI_CHECKLIST_ITEM_TEMPLATE_REF, c.getChecklistItemTemplateRef());
    	vals.put(CTG_RCI_QUESTION, c.getQuestion());
    	vals.put(CTG_RCI_TYPE, c.getType());
    	vals.put(CTG_RCI_EXTRA, c.getExtra());
    	vals.put(CTG_RCI_SECTION_ORDER, c.getSectionOrder());
    	vals.put(CTG_RCI_SECTION_INDEX, c.getSectionIndex());
    	vals.put(CTG_RCI_SECTION_NAME, c.getSectionName());
    	vals.put(CTG_RCI_META_STATUS, c.getMeta_status());
    	vals.put(CTG_RCI_BY_USER, c.getByUser());
    	vals.put(CTG_RCI_UPLOAD_DATE, c.getUploadDatetime());
    	vals.put(CTG_RCI_VALUE, c.getValue());
    	vals.put(CTG_RCI_COMMENT, c.getComment());
    	vals.put(CTG_RCI_CLIENT_RC_REF_INDEX, c.getClientRunChecklistRefIndex());
    	vals.put(CTG_RCI_CLIENT_CIT_REF_INDEX, c.getClientChecklistItemTemplateRefIndex());
    	vals.put(CTG_RCI_CLIENT_INDEX, c.getClientIndex());
    	vals.put(CTG_RCI_CLIENT_UUID, c.getClientUUID());
    	vals.put(CTG_RCI_LOCALLY_CHANGED, c.getLocally_changed());
		return vals;
    }

    	
	/**
	 * Get the data for the item currently pointed at by the database
	 * @param record
	 * @return
	 */
	public CTGRunChecklistItem getFromCursor(Cursor record) {
		CTGRunChecklistItem c= new CTGRunChecklistItem();
		
		if (record.getColumnCount() > 1){ //make sure data is in the result.  Read only first entry
	    	c.setId(record.getInt(CTG_RCI_ID_COL));
	    	c.setRunChecklistRef(record.getInt(CTG_RCI_RUN_CHECKLIST_REF_COL));
			c.setChecklistItemTemplateRef(record.getInt(CTG_RCI_CHECKLIST_ITEM_TEMPLATE_REF_COL));
			c.setQuestion(record.getString(CTG_RCI_QUESTION_COL));
			c.setType(record.getString(CTG_RCI_TYPE_COL));
			c.setExtra(record.getString(CTG_RCI_EXTRA_COL));
			c.setSectionOrder(record.getInt(CTG_RCI_SECTION_ORDER_COL));
			c.setSectionIndex(record.getInt(CTG_RCI_SECTION_INDEX_COL));
			c.setSectionName(record.getString(CTG_RCI_SECTION_NAME_COL));
			c.setMeta_status(record.getInt(CTG_RCI_META_STATUS_COL));
			c.setByUser(record.getInt(CTG_RCI_BY_USER_COL));
			c.setUploadDatetime(record.getString(CTG_RCI_UPLOAD_DATE_COL));
			c.setValue(record.getString(CTG_RCI_VALUE_COL));
			c.setComment(record.getString(CTG_RCI_COMMENT_COL));
			c.setClientRunChecklistRefIndex(record.getInt(CTG_RCI_CLIENT_RC_REF_INDEX_COL));
			c.setClientChecklistItemTemplateRefIndex(record.getInt(CTG_RCI_CLIENT_CIT_REF_INDEX_COL));
			c.setClientIndex(record.getInt(CTG_RCI_CLIENT_INDEX_COL));
			c.setClientUUID(record.getString(CTG_RCI_CLIENT_UUID_COL));
			c.setLocally_changed(record.getInt(CTG_RCI_LOCALLY_CHANGED_COL));
		}
		return c;
	}

    // ================------------> helper class <-----------==============\\
	private static class TableDbHelper extends SQLiteOpenHelper {

		public TableDbHelper(Context context, String name,
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
