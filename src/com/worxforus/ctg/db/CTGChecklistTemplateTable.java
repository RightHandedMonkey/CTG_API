package com.worxforus.ctg.db;

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
import com.worxforus.ctg.CTGChecklistItemTemplate;
import com.worxforus.ctg.CTGChecklistTemplate;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.ctg.CTGRunChecklistItem;
import com.worxforus.db.TableInterface;

public class CTGChecklistTemplateTable extends TableInterface<CTGChecklistTemplate> {

	public static final String DATABASE_NAME = CTGConstants.DATABASE_NAME;
	public static final String DATABASE_TABLE = "ctg_checklist_template_table";
	public static final int TABLE_VERSION = 1;
	// 1 - Initial version

	static int i = 0; // counter for field index
	public static final String CTG_CT_ID = "ctg_ct_id"; // int
	public static final int CTG_CT_ID_COL = i++;
	public static final String CTG_CT_TITLE = "ctg_ct_title"; // varchar(256)
	public static final int CTG_CT_TITLE_COL = i++;
	public static final String CTG_CT_DESC = "ctg_ct_desc"; // text
	public static final int CTG_CT_DESC_COL = i++;
	public static final String CTG_CT_META_STATUS = "ctg_ct_meta_status"; // int
	public static final int CTG_CT_META_STATUS_COL = i++;
	public static final String CTG_CT_BY_USER = "ctg_ct_by_user"; // int
	public static final int CTG_CT_BY_USER_COL = i++;
	public static final String CTG_CT_UPLOAD_DATE = "ctg_ct_upload_date"; // TEXT
	public static final int CTG_CT_UPLOAD_DATE_COL = i++;
	public static final String CTG_CT_SHARED = "ctg_ct_shared"; // INT
	public static final int CTG_CT_SHARED_COL = i++;
	public static final String CTG_CT_CAT1_ID = "ctg_ct_cat1_id"; // INT
	public static final int CTG_CT_CAT1_ID_COL = i++;
	public static final String CTG_CT_CAT1_NAME = "ctg_ct_cat1_name"; // VARCHAR(256)
	public static final int CTG_CT_CAT1_NAME_COL = i++;
	public static final String CTG_CT_CAT2_ID = "ctg_ct_cat2_id"; // INT
	public static final int CTG_CT_CAT2_ID_COL = i++;
	public static final String CTG_CT_CAT2_NAME = "ctg_ct_cat2_name"; // VARCHAR(256)
	public static final int CTG_CT_CAT2_NAME_COL = i++;
	public static final String CTG_CT_CLIENT_INDEX = "ctg_ct_client_index"; // INT
	public static final int CTG_CT_CLIENT_INDEX_COL = i++;
	public static final String CTG_CT_CLIENT_UUID = "ctg_ct_client_uuid"; // VARCHAR(64)
	public static final int CTG_CT_CLIENT_UUID_COL = i++;
	public static final String CTG_CT_LOCALLY_CHANGED = "ctg_ct_locally_changed"; // INTEGER NOT NULL DEFAULT 0
	public static final int CTG_CT_LOCALLY_CHANGED_COL = i++;

	public static final int MAX_NAME_LENGTH = 256;
	
	//db migrations
//	public static final String CTG_TAG_LOCALLY_CHANGED_TYPE = "INTEGER NOT NULL DEFAULT 0";

	private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " ( " 
			+ CTG_CT_ID + " 				INTEGER NOT NULL DEFAULT 0,"
			+ CTG_CT_TITLE + "    			TEXT NOT NULL DEFAULT ''," 
			+ CTG_CT_DESC + "    			TEXT," 
			+ CTG_CT_META_STATUS + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CT_BY_USER + "    		INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CT_UPLOAD_DATE + "    	TEXT NOT NULL DEFAULT ''," 
			+ CTG_CT_SHARED + "    			INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CT_CAT1_ID + "    		INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CT_CAT1_NAME + "    		TEXT NOT NULL DEFAULT ''," 
			+ CTG_CT_CAT2_ID + "    		INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CT_CAT2_NAME + "    		TEXT NOT NULL DEFAULT ''," 
			+ CTG_CT_CLIENT_INDEX + "    	INTEGER NOT NULL DEFAULT 0," 
			+ CTG_CT_CLIENT_UUID + "    	TEXT NOT NULL DEFAULT ''," 
			+ CTG_CT_LOCALLY_CHANGED + "    INTEGER NOT NULL DEFAULT 0," 
			+ " PRIMARY KEY(" + CTG_CT_ID + ", "
			+ CTG_CT_CLIENT_INDEX + ", " + CTG_CT_CLIENT_UUID + " ) " + 
			")";

	// NOTE: When adding to the table items added locally: i.e. no id field, the
	// client needs to put in the client_uuid and client_index
	// Something like: INSERT INTO Log (id, rev_no, description) VALUES ((SELECT
	// IFNULL(MAX(id), 0)) + 1 FROM Log), 'rev_Id', 'some description')

	private static final String INDEX_1_NAME = DATABASE_TABLE+"_index_1";//NOTE: Indexes must be unique across all tables in db
	private static final String INDEX_1 = "CREATE INDEX " + INDEX_1_NAME
			+ " ON " + DATABASE_TABLE + " (  `" + CTG_CT_TITLE + "`, `"+ CTG_CT_META_STATUS + "`, `"
			+ CTG_CT_SHARED + "`, `" + CTG_CT_CAT1_ID + "`, `"
			+ CTG_CT_BY_USER + "`, `" + CTG_CT_ID + "`, `"
			+ CTG_CT_CAT2_ID + "` )";

	private SQLiteDatabase db;
	// holds the app using the db
	private CTGTagTableDbHelper dbHelper;

//	protected int last_version = 0;

	public CTGChecklistTemplateTable(Context _context) {
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
	public Result insertOrUpdate(CTGChecklistTemplate t) {
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
		return db.delete(DATABASE_TABLE, CTG_CT_ID+" = 0 AND "+CTG_CT_CLIENT_INDEX+" = ? AND "+CTG_CT_CLIENT_UUID+" = ?", new String[] {clientIndex+"", clientUUID});
	}
	
	private SQLiteStatement bindToReplace(SQLiteStatement stmt, CTGChecklistTemplate ct) {
		stmt.clearBindings();
		stmt.bindLong(CTG_CT_ID_COL+1, ct.getId());
		stmt.bindString(CTG_CT_TITLE_COL+1, ct.getTitle());
		stmt.bindString(CTG_CT_DESC_COL+1, ct.getDesc());
		stmt.bindLong(CTG_CT_META_STATUS_COL+1, ct.getMeta_status());
		stmt.bindLong(CTG_CT_BY_USER_COL+1, ct.getBy_user());
		stmt.bindString(CTG_CT_UPLOAD_DATE_COL+1, ct.getUpload_datetime());
		stmt.bindLong(CTG_CT_SHARED_COL+1, ct.getShared());
		stmt.bindLong(CTG_CT_CAT1_ID_COL+1, ct.getCat1_id());
		stmt.bindString(CTG_CT_CAT1_NAME_COL+1, ct.getCat1_name());
		stmt.bindLong(CTG_CT_CAT2_ID_COL+1, ct.getCat2_id());
		stmt.bindString(CTG_CT_CAT2_NAME_COL+1, ct.getCat2_name());
		stmt.bindLong(CTG_CT_CLIENT_INDEX_COL+1, ct.getClient_index());
		stmt.bindString(CTG_CT_CLIENT_UUID_COL+1, ct.getClient_uuid());
		stmt.bindLong(CTG_CT_LOCALLY_CHANGED_COL+1, ct.getLocally_changed());
		return stmt;

	}

	
	public Result insertOrUpdateArrayList(ArrayList<CTGChecklistTemplate> list) {
		Result r = new Result();
		String sql = "INSERT OR REPLACE INTO "+DATABASE_TABLE+" VALUES(?,?,?,?,?,  ?,?,?,?,?, ?,?,?,?) "; //15 items
		SQLiteStatement statement = db.compileStatement(sql);
		beginTransaction();
		for (CTGChecklistTemplate item : list) {
			bindToReplace(statement, item);
			try {
				statement.execute();	
				if (item.getId() > 0 && item.getClient_index() > 0) {
					removeLocallyCreatedItem(item.getClient_index(), item.getClient_uuid());
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
	
	/*
	public Result insertOrUpdateArrayList(ArrayList<CTGChecklistTemplate> t) {
		Result r = new Result();
		beginTransaction();
		for (CTGChecklistTemplate ctgTag : t) {
			r.add_results_if_error(insertOrUpdate(ctgTag), "Could not add CTGChecklistTemplate "+t+" to database." );
		}
		endTransaction();
		return r;
	}
*/
	public Result createLocal(CTGChecklistTemplate ct) {
		synchronized (DATABASE_TABLE) {
			int rowId = -1;
			Result r = new Result();
			try {
				//get latest value of clientIndex and add 1
				int nextIndex = getNextClientIndex(ct.getClient_uuid());
				ct.setClient_index(nextIndex);
				Assert.assertTrue("Could not get the next Checklist Template client index for uuid: "+ct.getClient_uuid(), nextIndex > 0);
				ContentValues cv = getContentValues(ct);
				rowId = (int) db.insert(DATABASE_TABLE, null, cv);
				Assert.assertTrue("Could not insert a locally created Checklist Template item: "+ct.toString(), rowId > 0);
			} catch( Exception e ) {
				Log.e(this.getClass().getName(), e.getMessage());
				r.error = e.getMessage();
				r.success = false;
			}
			if (rowId < 1) {
				r.technical_error = "Could not add ChecklistTemplate in db. Data: "+ct.toString();
				r.success = false;
			}
			return r;
		}
	}
	
	public int getNextClientIndex(String uuid) {
		synchronized(this) {
			int i=1;
			Cursor c = db.rawQuery("SELECT MAX("+CTG_CT_CLIENT_INDEX+") FROM "+ DATABASE_TABLE+" WHERE "+CTG_CT_CLIENT_UUID+" = ? ", 
					new String[] { uuid});
			if (c.moveToFirst()){
				i = c.getInt(0)+1;
			}
			c.close();
			return i;
		}
	}
	public CTGChecklistTemplate getEntry(int id) {
		//String where = KEY_NUM+" = "+user_num;
		CTGChecklistTemplate c= new CTGChecklistTemplate();
		Cursor result= db.query(DATABASE_TABLE, 
				null, 
				CTG_CT_ID+" = ? ", new String[] {id+""}, null, null, null);
		if (result.moveToFirst() ) { //make sure data is in the result.  Read only first entry
			c = getFromCursor(result);
		}
		result.close();
		return c;
	}
	
	
	public ArrayList<CTGChecklistTemplate> getTemplatesByCat1(int cat1Id) {
		ArrayList<CTGChecklistTemplate> al = new ArrayList<CTGChecklistTemplate>();
		Cursor list = getTemplatesByCat1Cursor(cat1Id);
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
	 * @return ArrayList<CTGChecklistTemplate>
	 */
	public Cursor getTemplatesByCat1Cursor(int cat1Id) {
		return db.query(DATABASE_TABLE, null, 
				CTG_CT_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL+" AND "+CTG_CT_CAT1_ID+" = "+cat1Id, null, null, null, CTG_CT_TITLE);
	}
	
//	public ArrayList<CTGChecklistTemplate> getUserTemplates(int userID) {
//		ArrayList<CTGChecklistTemplate> al = new ArrayList<CTGChecklistTemplate>();
//		Cursor list = getUserTemplatesCursor(userID);
//		if (list.moveToFirst()){
//			do {
//				al.add(getFromCursor(list));
//			} while(list.moveToNext());
//		}
//		list.close();
//		return al;
//	}
	
	public ArrayList<CTGChecklistTemplate> getUserTemplates(int userID, String uuid) {
		ArrayList<CTGChecklistTemplate> al = new ArrayList<CTGChecklistTemplate>();
		Cursor list = getUserTemplatesCursor(userID, uuid);
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
//	 * @return ArrayList<CTGChecklistTemplate>
//	 */
//	public Cursor getUserTemplatesCursor(int userID) {
//		return db.query(DATABASE_TABLE, null, 
//				CTG_CT_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL+" AND "+CTG_CT_BY_USER+" = "+userID, null, null, null, CTG_CT_TITLE);
//	}
	
	/**
	 * Returns the cursor objects.
	 * @return ArrayList<CTGChecklistTemplate>
	 */
	public Cursor getUserTemplatesCursor(int userID, String uuid) {
		return db.query(DATABASE_TABLE, null, 
			CTG_CT_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL+" AND ( "+CTG_CT_BY_USER+" = "+userID+" OR "+CTG_CT_CLIENT_UUID+" = ? )", new String[] {uuid}, null, null, CTG_CT_TITLE);
	}
	
	
	public ArrayList<CTGChecklistTemplate> getValidEntries() {
		ArrayList<CTGChecklistTemplate> al = new ArrayList<CTGChecklistTemplate>();
		Cursor list = getValidEntriesCursor();
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
	 * @return ArrayList<CTGChecklistTemplate>
	 */
	public Cursor getValidEntriesCursor() {
		return db.query(DATABASE_TABLE, null, 
				CTG_CT_META_STATUS+" = "+CTGConstants.META_STATUS_NORMAL, null, null, null, CTG_CT_TITLE);
	}
	
	/**
	 * Retrieve all entries for testing purposes
	 * @return ArrayList<CTGChecklistTemplate>
	 */
	public ArrayList<CTGChecklistTemplate> getAllEntries() {
		ArrayList<CTGChecklistTemplate> al = new ArrayList<CTGChecklistTemplate>();
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
		return db.query(DATABASE_TABLE, null, null, null, null, null, CTG_CT_ID);
	}
	
	
	// ================------------> helpers <-----------==============\\
    /** returns a ContentValues object for database insertion
     * 
     * @return
     */
    public ContentValues getContentValues(CTGChecklistTemplate c) {
    	ContentValues vals = new ContentValues();
    	//prepare info for db insert/update
    	vals.put(CTG_CT_ID, c.getId());
    	vals.put(CTG_CT_TITLE, c.getTitle());
    	vals.put(CTG_CT_DESC, c.getDesc());
    	vals.put(CTG_CT_META_STATUS, c.getMeta_status());
    	vals.put(CTG_CT_BY_USER, c.getBy_user());
    	vals.put(CTG_CT_UPLOAD_DATE, c.getUpload_datetime());
    	vals.put(CTG_CT_SHARED, c.getShared());
    	vals.put(CTG_CT_CAT1_ID, c.getCat1_id());
    	vals.put(CTG_CT_CAT1_NAME, c.getCat1_name());
    	vals.put(CTG_CT_CAT2_ID, c.getCat2_id());
    	vals.put(CTG_CT_CAT2_NAME, c.getCat2_name());
    	vals.put(CTG_CT_CLIENT_INDEX, c.getClient_index());
    	vals.put(CTG_CT_CLIENT_UUID, c.getClient_uuid());
    	vals.put(CTG_CT_LOCALLY_CHANGED, c.getLocally_changed());
		return vals;
    }

	/**
	 * Get the data for the item currently pointed at by the database
	 * @param record
	 * @return
	 */
	public CTGChecklistTemplate getFromCursor(Cursor record) {
		CTGChecklistTemplate c= new CTGChecklistTemplate();
		
		if (record.getColumnCount() > 1){ //make sure data is in the result.  Read only first entry
	    	c.setId(record.getInt(CTG_CT_ID_COL));
			c.setTitle(record.getString(CTG_CT_TITLE_COL));
			c.setDesc(record.getString(CTG_CT_DESC_COL));
			c.setMeta_status(record.getInt(CTG_CT_META_STATUS_COL));
			c.setBy_user(record.getInt(CTG_CT_BY_USER_COL));
			c.setUpload_datetime(record.getString(CTG_CT_UPLOAD_DATE_COL));
			c.setShared(record.getInt(CTG_CT_SHARED_COL));
			c.setCat1_id(record.getInt(CTG_CT_CAT1_ID_COL));
			c.setCat1_name(record.getString(CTG_CT_CAT1_NAME_COL));
			c.setCat2_id(record.getInt(CTG_CT_CAT2_ID_COL));
			c.setCat2_name(record.getString(CTG_CT_CAT2_NAME_COL));
			c.setClient_index(record.getInt(CTG_CT_CLIENT_INDEX_COL));
			c.setClient_uuid(record.getString(CTG_CT_CLIENT_UUID_COL));
			c.setLocally_changed(record.getInt(CTG_CT_LOCALLY_CHANGED_COL));
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
