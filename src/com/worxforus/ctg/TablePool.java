package com.worxforus.ctg;

import junit.framework.Assert;

import android.app.Application;
import android.content.Context;

import com.worxforus.ctg.db.CTGTagTable;
import com.worxforus.db.TableVersionDb;

/*
 * - Database helper singleton style
 * 
 * QuizDBConnector.Connect(Context c); 
 * QuizDBConnector.getDbX();
 * QuizDBConnector.Release();
 */

public class TablePool {
	//private Application application;
	private static TablePool instance = new TablePool();

	// databases - these don't need to be static because they will be called from the static instance
	private CTGTagTable tagTable;
	private TableVersionDb tableVersionTable;

	public TablePool() {
	}

	public static TablePool self() {
		return instance;
	}

	public static CTGTagTable getTagTable(Context appContext) {
		Assert.assertNotNull(appContext);
		if (self().tagTable == null) {
			self().tagTable = new CTGTagTable(appContext);
		}
		return self().tagTable;
	}

	public static TableVersionDb getTableVersions(Context appContext) {
		if (self().tableVersionTable == null) {
			self().tableVersionTable = new TableVersionDb(appContext, com.worxforus.ctg.CTGConstants.DATABASE_NAME);
		}
		return self().tableVersionTable;
	}
	
}