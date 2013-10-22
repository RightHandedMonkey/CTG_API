package com.worxforus.ctg;

import junit.framework.Assert;
import android.content.Context;

import com.worxforus.ctg.db.CTGChecklistItemTemplateTable;
import com.worxforus.ctg.db.CTGChecklistTemplateTable;
import com.worxforus.ctg.db.CTGRunChecklistItemTable;
import com.worxforus.ctg.db.CTGRunChecklistTable;
import com.worxforus.ctg.db.CTGTagTable;
import com.worxforus.db.TableVersionDb;

/*
 * - Database table holder - singleton style
 * Usage:
 * TagTable tagTable = TablePool.getTagTable(getActivity());
 */

public class TablePool {
	//private Application application;
	private static TablePool instance = new TablePool();

	// databases - these don't need to be static because they will be called from the static instance
	private TableVersionDb tableVersionTable;

	private CTGTagTable tagTable;
	private CTGChecklistTemplateTable ctTable;
	private CTGChecklistItemTemplateTable citTable;
	private CTGRunChecklistTable rcTable;
	private CTGRunChecklistItemTable rciTable;

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
			self().tableVersionTable = new TableVersionDb(appContext, CTGConstants.DATABASE_NAME);
		}
		return self().tableVersionTable;
	}

	public static CTGChecklistTemplateTable getCTTable(Context appContext) {
		Assert.assertNotNull(appContext);
		if (self().ctTable == null) {
			self().ctTable = new CTGChecklistTemplateTable(appContext);
		}
		return self().ctTable;	
	}

	public static CTGChecklistItemTemplateTable getCITTable(Context appContext) {
		Assert.assertNotNull(appContext);
		if (self().citTable == null) {
			self().citTable = new CTGChecklistItemTemplateTable(appContext);
		}
		return self().citTable;	
	}
	
	public static CTGRunChecklistTable getRCTable(Context appContext) {
		Assert.assertNotNull(appContext);
		if (self().rcTable == null) {
			self().rcTable = new CTGRunChecklistTable(appContext);
		}
		return self().rcTable;	
	}

	public static CTGRunChecklistItemTable getRCITable(Context appContext) {
		Assert.assertNotNull(appContext);
		if (self().rciTable == null) {
			self().rciTable = new CTGRunChecklistItemTable(appContext);
		}
		return self().rciTable;	
	}
	
}