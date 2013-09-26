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

import com.worxforus.Constants;
import com.worxforus.Result;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.ctg.CTGTag;
import com.worxforus.db.TableInterface;

public class CTGTagTable extends TableInterface<CTGTag> {

	public static final String DATABASE_NAME = CTGConstants.DATABASE_NAME;
	public static final String DATABASE_TABLE = "ctg_tag";
	public static final int TABLE_VERSION = 3; //Updated Index and added 'locally changed'
	// 2 - Added index
	// 1 - Initial version

	static int i = 0; // counter for field index
	public static final String CTG_TAG_ID = "ctg_tag_id"; // int
	public static final int CTG_TAG_ID_COL = i++;
	public static final String CTG_TAG_NAME = "ctg_tag_name"; // varchar(256)
	public static final int CTG_TAG_NAME_COL = i++;
	public static final String CTG_TAG_DESC = "ctg_tag_desc"; // text
	public static final int CTG_TAG_DESC_COL = i++;
	public static final String CTG_PARENT_TAG_REF = "ctg_parent_tag_ref"; // int
	public static final int CTG_PARENT_TAG_REF_COL = i++;
	public static final String CTG_META_STATUS = "ctg_meta_status"; // int
	public static final int CTG_META_STATUS_COL = i++;
	public static final String CTG_TAG_UPLOAD_DATE = "ctg_tag_upload_date"; // DATETIME
	public static final int CTG_TAG_UPLOAD_DATE_COL = i++;
	public static final String CTG_TAG_CLIENT_INDEX = "ctg_tag_client_index"; // INT
	public static final int CTG_TAG_CLIENT_INDEX_COL = i++;
	public static final String CTG_TAG_CLIENT_UUID = "ctg_tag_client_uuid"; // VARCHAR([UUID_SIZE])
	public static final int CTG_TAG_CLIENT_UUID_COL = i++;
	public static final String CTG_TAG_LOCALLY_CHANGED = "ctg_tag_locally_changed"; // INT
	public static final int CTG_TAG_LOCALLY_CHANGED_COL = i++;

	public static final int MAX_NAME_LENGTH = 256;
	
	//db migrations
	public static final String CTG_TAG_LOCALLY_CHANGED_TYPE = "INTEGER NOT NULL DEFAULT 0";

	private static final String DATABASE_CREATE = "CREATE TABLE "
			+ DATABASE_TABLE + " ( " + CTG_TAG_ID + " 				INTEGER NOT NULL,"
			+ CTG_TAG_NAME + "    			TEXT NOT NULL DEFAULT ''," + CTG_TAG_DESC
			+ "   			TEXT," + CTG_PARENT_TAG_REF
			+ "      	INTEGER NOT NULL DEFAULT 0," + CTG_META_STATUS
			+ "    		INTEGER NOT NULL DEFAULT " + Constants.META_STATUS_NORMAL
			+ "," + CTG_TAG_UPLOAD_DATE + "       TEXT," + CTG_TAG_CLIENT_INDEX
			+ "      INTEGER NOT NULL DEFAULT 0," + CTG_TAG_CLIENT_UUID
			+ "    	TEXT DEFAULT ''," +CTG_TAG_LOCALLY_CHANGED+" "+CTG_TAG_LOCALLY_CHANGED_TYPE+", "
			+ " PRIMARY KEY(" + CTG_TAG_ID + ", "
			+ CTG_TAG_CLIENT_INDEX + ", " + CTG_TAG_CLIENT_UUID + " ) " + 
			")";
	// NOTE: When adding to the table items added locally: i.e. no id field, the
	// client needs to put in the client_uuid and client_index
	// Something like: INSERT INTO Log (id, rev_no, description) VALUES ((SELECT
	// IFNULL(MAX(id), 0)) + 1 FROM Log), 'rev_Id', 'some description')

	private static final String INDEX_1_NAME = DATABASE_TABLE+"_index_1";//NOTE: Indexes must be unique across all tables in db
	private static final String INDEX_1 = "CREATE INDEX " + INDEX_1_NAME
			+ " ON " + DATABASE_TABLE + " (  `" + CTG_TAG_NAME + "`, `"+ CTG_TAG_LOCALLY_CHANGED + "`, `"
			+ CTG_META_STATUS + "`, `" + CTG_TAG_UPLOAD_DATE + "`, `"
			+ CTG_TAG_ID + "`, `" + CTG_TAG_CLIENT_INDEX + "`, `"
			+ CTG_TAG_CLIENT_UUID + "` )";

	private SQLiteDatabase db;
	// holds the app using the db
	private CTGTagTableDbHelper dbHelper;

//	protected int last_version = 0;

	public CTGTagTable(Context _context) {
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
	public Result insertOrUpdate(CTGTag t) {
		synchronized (DATABASE_TABLE) {
			int index = -1;
			Result r = new Result();
			try {
				ContentValues cv = getContentValues(t);
				index = (int) db.replace(DATABASE_TABLE, null, cv);
				r.last_insert_id = index;
				//if id > 0 && client_index > 0 - we need to check if we need to delete a locally created object
				if (t.getId() > 0 && t.getClient_index() > 0) {
					//delete any locally created objects matching id=0, client_index=x, client_uuid=y
					removeLocallyCreatedItem(t.getClient_index(), t.getClient_uuid());
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

	public int removeLocallyCreatedItem(int clientIndex, String clientUUID) {
		return db.delete(DATABASE_TABLE, CTG_TAG_ID+" = 0 AND "+CTG_TAG_CLIENT_INDEX+" = ? AND "+CTG_TAG_CLIENT_UUID+" = ?", new String[] {clientIndex+"", clientUUID});
	}
	
	public Result insertOrUpdateArrayList(ArrayList<CTGTag> t) {
		Result r = new Result();
		beginTransaction();
		for (CTGTag ctgTag : t) {
			r.add_results_if_error(insertOrUpdate(ctgTag), "Could not add CTGTag "+t+" to database." );
		}
		endTransaction();
		return r;
	}

	public CTGTag getEntry(int tagId) {
		//String where = KEY_NUM+" = "+user_num;
		CTGTag c= new CTGTag();
		Cursor result= db.query(DATABASE_TABLE, 
				null, 
				CTG_TAG_ID+" = ? ", new String[] {tagId+""}, null, null, null);
		if (result.moveToFirst() ) { //make sure data is in the result.  Read only first entry
			c = getFromCursor(result);
		}
		result.close();
		return c;
	}
	
	public ArrayList<CTGTag> getValidRootEntries() {
		ArrayList<CTGTag> al = new ArrayList<CTGTag>();
		Cursor list = getValidRootEntriesCursor();
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
	 * if flatten is selected returns only one item for each project (no duplicates) 
	 * @return ArrayList<CTGTag>
	 */
	public Cursor getValidRootEntriesCursor() {
		return db.query(DATABASE_TABLE, null, 
				CTG_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL+" AND "+CTG_PARENT_TAG_REF+" = 0", null, null, null, CTG_TAG_NAME);
	}
	
	/**
	 * Retrieve all entries for testing purposes
	 * @return ArrayList<CTGTag>
	 */
	public ArrayList<CTGTag> getAllEntries() {
		ArrayList<CTGTag> al = new ArrayList<CTGTag>();
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
		return db.query(DATABASE_TABLE, null, null, null, null, null, CTG_TAG_NAME);
	}
	
	
	// ================------------> helpers <-----------==============\\
    /** returns a ContentValues object for database insertion
     * 
     * @return
     */
    public ContentValues getContentValues(CTGTag c) {
    	ContentValues vals = new ContentValues();
    	//prepare info for db insert/update
    	vals.put(CTG_TAG_ID, c.getId());
    	vals.put(CTG_TAG_NAME, c.getName());
    	vals.put(CTG_TAG_DESC, c.getDesc());
    	vals.put(CTG_PARENT_TAG_REF, c.getParent_ref());
    	vals.put(CTG_META_STATUS, c.getMeta_status());
    	vals.put(CTG_TAG_UPLOAD_DATE, c.getUpload_datetime());
    	vals.put(CTG_TAG_CLIENT_INDEX, c.getClient_index());
    	vals.put(CTG_TAG_CLIENT_UUID, c.getClient_uuid());
    	vals.put(CTG_TAG_LOCALLY_CHANGED, c.getLocally_changed());
		return vals;
    }

	/**
	 * Get the data for the item currently pointed at by the database
	 * @param record
	 * @return
	 */
	public CTGTag getFromCursor(Cursor record) {
		CTGTag c= new CTGTag();
		
		if (record.getColumnCount() > 8){ //make sure data is in the result.  Read only first entry
	    	c.setId(record.getInt(CTG_TAG_ID_COL));
			c.setName(record.getString(CTG_TAG_NAME_COL));
			c.setDesc(record.getString(CTG_TAG_DESC_COL));
			c.setParent_ref(record.getInt(CTG_PARENT_TAG_REF_COL));
			c.setMeta_status(record.getInt(CTG_META_STATUS_COL));
			
			c.setUpload_datetime(record.getString(CTG_TAG_UPLOAD_DATE_COL));
			c.setClient_index(record.getInt(CTG_TAG_CLIENT_INDEX_COL));
			c.setClient_uuid(record.getString(CTG_TAG_CLIENT_UUID_COL));
			c.setLocally_changed(record.getInt(CTG_TAG_LOCALLY_CHANGED_COL));
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
			if (oldVersion < 1) { // if old version was V1, just add field
				// create new table
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
				onCreate(db);
				Log.d(this.getClass().getName(), "Creating new "+ DATABASE_TABLE + " Table");
			}
			if (oldVersion < 3) {
				//add locally changed field
				db.execSQL("ALTER TABLE "+DATABASE_TABLE+" ADD COLUMN "+CTG_TAG_LOCALLY_CHANGED+" "+CTG_TAG_LOCALLY_CHANGED_TYPE);
				db.execSQL("DROP INDEX IF EXISTS "+INDEX_1_NAME);
				db.execSQL(INDEX_1);
				Log.d(this.getClass().getName(), "Adding new field and new index to "	+ DATABASE_TABLE + " Table");
			}
		}

	}


}
