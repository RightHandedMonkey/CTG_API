package com.worxforus.ctg;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import junit.framework.Assert;
import android.content.Context;

import com.worxforus.Utils;
import com.worxforus.ctg.db.CTGChecklistItemTemplateTable;
import com.worxforus.ctg.db.CTGChecklistTemplateTable;
import com.worxforus.ctg.db.CTGRunChecklistItemTable;
import com.worxforus.ctg.db.CTGRunChecklistTable;
import com.worxforus.ctg.db.CTGTagTable;
import com.worxforus.db.TableManager;
import com.worxforus.db.TablePool;
import com.worxforus.db.TableVersionDb;

/*
 * - Database table holder - singleton style
 * Usage:
 * CTGTablePool.setDbName("AppDb");
 * TagTable tagTable = CTGTablePool.getTable(CTGTagTable.class, getActivity());
 */

public class CTGTablePool {
	//private Application application;
	private static CTGTablePool instance = new CTGTablePool();
	private HashMap tableMap;
	private String dbName;

	// databases - these don't need to be static because they will be called from the static instance
	private TableVersionDb tableVersionTable;

	public static TableVersionDb getTableVersions(Context appContext) {
		self().checkInit();
		if (self().tableVersionTable == null) {
			self().tableVersionTable = new TableVersionDb(appContext.getApplicationContext(), self().dbName);
		}
		return self().tableVersionTable;
	}

	private CTGTablePool() {
		tableMap = new HashMap();
	}

	public static CTGTablePool self() {
		return instance;
	}
	
	/**
	 * Set or change the name of the database to be accessed
	 * This also resets the table pool
	 * @param name
	 */
	public synchronized static void setDbName(String name) {
		Utils.LogD(CTGTablePool.class.getName(), "Changing db connection to: "+name);
		self().dbName = name;
		invalidate();
	}
	
	public static String getDbName() {
		self().checkInit();
		return self().dbName;
	}
	
	private void checkInit() {
		Assert.assertNotNull(CTGTablePool.class.getName()+".setDbName() needs to be called first!", self().dbName);
	}
	
	/**
	 * 
	 * @param tableClass
	 * @param c - Use application context if your process lives longer than the activity
	 * @return
	 */
	public synchronized static <T> T getTable(Class<T> tableClass, Context c) {
		self().checkInit();
		String hashKey = self().dbName+tableClass.getName();
		if (self().tableMap.get(hashKey) == null) {
			Utils.LogD(TablePool.class.getName(), "Creating table connection for: "+tableClass.getName()+" in db: "+self().dbName);

			Constructor<T> construct;
			T tableInstance = null;
			try {
				construct = tableClass.getConstructor(Context.class, String.class);
				tableInstance = (T) construct.newInstance(c.getApplicationContext(), self().dbName);
				self().tableMap.put(hashKey, tableInstance);
			} catch (Exception e) {
				throw new RuntimeException("Could not create the table: "+tableClass.getName()+" in db: "+self().dbName+", because of: "+e.toString());
			}
			return tableInstance;
		} else {
			Utils.LogD(TablePool.class.getName(), "Reusing existing table connection for: "+tableClass.getName()+" in db: "+self().dbName);
			return (T) self().tableMap.get(hashKey);
		}
	}
	
	/**
	 * Reset all the database connections
	 */
	public synchronized static void invalidate() {
		Utils.LogD(CTGTablePool.class.getName(), "Invalidating database connections");
		self().tableMap.clear();
		self().tableVersionTable = null;
		TableManager.invalidate();
	}
}