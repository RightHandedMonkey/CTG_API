package com.worxforus.ctg;

import java.util.ArrayList;

import junit.framework.Assert;
import android.content.Context;

import com.worxforus.Result;
import com.worxforus.SyncEntry;
import com.worxforus.Utils;
import com.worxforus.app.CITListNotifier;
import com.worxforus.app.CTListNotifier;
import com.worxforus.app.RCIListNotifier;
import com.worxforus.app.RCListNotifier;
import com.worxforus.app.TagListNotifier;
import com.worxforus.ctg.db.CTGChecklistItemTemplateTable;
import com.worxforus.ctg.db.CTGChecklistTemplateTable;
import com.worxforus.ctg.db.CTGRunChecklistItemTable;
import com.worxforus.ctg.db.CTGRunChecklistTable;
import com.worxforus.ctg.db.CTGTagTable;
import com.worxforus.db.TableManager;
import com.worxforus.db.TableSyncDb;
import com.worxforus.net.SyncTableManager;

/**
 * This class is used to help in the creation of checklists on a client device.
 * It works with items created locally on a client device before being sent to the server, therefore
 * all the ids for items will be 0 and instead will use the client ids to uniquely identify items.
 * 
 * NOTE: All items on the server have an id.  This is not true for items created locally on a device, their id will be 0
 * @author sbossen
 *
 */
public class CTGChecklistInterface {

	/**
	 * Creates a new run_checklist object from a template
	 * Returns: Result.object - which is a reference to the actual created CTGRunChecklist object
	 * @param template
	 * @param uuid
	 * @param context
	 * @return result
	 */
	public static Result createRunChecklistFromTemplate(CTGChecklistTemplate template, String uuid, Context context) {
		Result result;
		CTGRunChecklist rc = new CTGRunChecklist();
		rc.update(); //mark as locally changed
		associateRCwithCT(template, rc);
		rc.setClientUUID(uuid);
		CTGRunChecklistTable rcTable = TablePool.getRCTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, rcTable);
		//check how many times the user has used this template to attempt to set title to a unique name
		int count = rcTable.getNumTimesTemplateUsed(template.getId());
		if(count > 0) {
			rc.setTitle(rc.getTitle()+" #"+count);
		}
		//Create the checklist and assign a new client index
		result = rcTable.createLocalRunChecklist(rc);
		if (result.success) {
			Assert.assertTrue(result.object instanceof CTGRunChecklist);
			rc = (CTGRunChecklist)result.object;
		} else {
			return result;
		}
		TableManager.releaseConnection(rcTable);
		//Now create the checklist items
		if (result.success) {
			result.add_results_if_error(CTGChecklistInterface.createRunItemsForChecklist(rc, context), "Could not create the items for this checklist.");
		}

		return result;
	}
	
	/**
	 * Creates a new template object from a run checklist
	 * Returns: Result.object - which is a reference to the actual created CTGChecklistTemplate object
	 * @param fromRC - Original checklist to base template on
	 * @param uuid
	 * @param context
	 * @return result
	 */
	public static Result createTemplateFromRunChecklist(CTGRunChecklist fromRC, String uuid, Context context) {
		Result result;
		CTGChecklistTemplate ct = new CTGChecklistTemplate();
		ct.setClient_uuid(uuid);
		ct.setBy_user(fromRC.getByUser());
		ct.setTitle(fromRC.getTitle());
		ct.update(); //mark as locally changed
		CTGChecklistTemplateTable ctTable = TablePool.getCTTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, ctTable);
		//Create the template which creates the new client index
		result = ctTable.createLocal(ct);
		if (result.success) {
			Assert.assertTrue(result.object instanceof CTGChecklistTemplate);
			ct = (CTGChecklistTemplate)result.object;
		} else {
			return result;
		}
		TableManager.releaseConnection(ctTable);
		//Now create the checklist item templates
		if (result.success) {
			result.add_results_if_error(CTGChecklistInterface.createTemplateItemsFromRunChecklist(fromRC, ct, context), "Could not create the items for this template.");
		}

		//now set the RC to be linked to the template we just created
		fromRC.setTemplateRef(ct.getId());
		fromRC.setClientRefIndex(ct.getClient_index());
		fromRC.update();
		CTGRunChecklistTable rcTable = TablePool.getRCTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, rcTable);
		rcTable.update(fromRC);
		TableManager.releaseConnection(rcTable);

		return result;
	}
	
	/**
	 * Given a run checklist instance , this function takes all the items from the template
	 * and copies them into the run checklist items for that run checklist
	 * NOTE: This should only be called on a brand new run checklist instance
	 * @param rc
	 * @param context
	 * @return
	 */
	public static Result createRunItemsForChecklist(CTGRunChecklist rc, Context context) {
		Result result; // = new Result();
		//The associated template and template items need to exist on this device
		CTGChecklistItemTemplateTable citTable = TablePool.getCITTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, citTable);
		ArrayList<CTGChecklistItemTemplate> templateItems = citTable.getValidTemplateItems(rc.getTemplateRef(), rc.getClientRefIndex(), rc.getClientUUID());
		TableManager.releaseConnection(citTable);
		
		//create all the CTGRunChecklistItem objects
		ArrayList<CTGRunChecklistItem> rciItems = new ArrayList<CTGRunChecklistItem>();
		
		for (CTGChecklistItemTemplate cit : templateItems) {
			CTGRunChecklistItem rci =  new CTGRunChecklistItem();
			rci.update(); //mark as locally updated.
			//fill in data from template
			CTGChecklistInterface.copyCITtoRCI(cit, rci);
			//associate with checklist
			associateRCIwithRC(rc, rci);
			rciItems.add(rci);
		}
		
		//now create all objects in db
		CTGRunChecklistItemTable rciTable = TablePool.getRCITable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, rciTable);
		result = rciTable.createLocalRunChecklistItems(rciItems);
		TableManager.releaseConnection(rciTable);
		return result;
	}
	
	/**
	 * Given a run checklist instance , this function takes all the items from the template
	 * and copies them into the run checklist items for that run checklist
	 * NOTE: This should only be called on a brand new run checklist instance
	 * @param fromRC
	 * @param context
	 * @return
	 */
	public static Result createTemplateItemsFromRunChecklist(CTGRunChecklist fromRC, CTGChecklistTemplate ct, Context context) {
		Result result = new Result();
		//The associated template and template items need to exist on this device
		CTGRunChecklistItemTable rciTable = TablePool.getRCITable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, rciTable);
		ArrayList<CTGRunChecklistItem> rciItems = rciTable.getValidChecklistItems(fromRC.getId(), fromRC.getClientIndex(), fromRC.getClientUUID());
		TableManager.releaseConnection(rciTable);

		//create all the CTGChecklistItemTemplate objects
		CTGChecklistItemTemplateTable citTable = TablePool.getCITTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, citTable);
		for (CTGRunChecklistItem rci : rciItems) {
			CTGChecklistItemTemplate cit =  new CTGChecklistItemTemplate();
			//fill in data from template
			CTGChecklistInterface.copyRCItoCIT(rci, cit);
			//associate with template
			associateCITwithCT(ct, cit);
			cit.update(); //mark as locally updated.
			//save the new checklist item templates to the db
			result.add_results_if_error(citTable.createLocal(cit), "");
		}
		TableManager.releaseConnection(citTable);
		return result;
	}
	

	public static void associateRCwithCT(CTGChecklistTemplate linkFrom, CTGRunChecklist linkTo) {
		linkTo.setTitle(linkFrom.getTitle());
		linkTo.setTemplateRef(linkFrom.getId());
		linkTo.setClientRefIndex(linkFrom.getClient_index());
	}
	
	public static void associateRCIwithRC(CTGRunChecklist linkFrom, CTGRunChecklistItem linkTo) {
		linkTo.setRunChecklistRef(linkFrom.getId());
		linkTo.setClientRunChecklistRefIndex(linkFrom.getClientIndex());
		linkTo.setClientUUID(linkFrom.getClientUUID());
	}
	
	public static void copyCITtoRCI(CTGChecklistItemTemplate copyFrom, CTGRunChecklistItem copyTo) {
		copyTo.setMeta_status(copyFrom.getMeta_status());
		copyTo.setChecklistItemTemplateRef(copyFrom.getTemplateRef());
		copyTo.setClientChecklistItemTemplateRefIndex(copyFrom.getClientIndex());
		copyTo.setQuestion(copyFrom.getQuestion());
		copyTo.setType(copyFrom.getType());
		copyTo.setExtra(copyFrom.getExtra());
		copyTo.setSectionOrder(copyFrom.getSectionOrder());
		copyTo.setSectionIndex(copyFrom.getSectionIndex());
		copyTo.setSectionName(copyFrom.getSectionName());
	}
	
	public static void copyRCItoCIT(CTGRunChecklistItem copyFrom, CTGChecklistItemTemplate copyTo) {
		copyTo.setMeta_status(copyFrom.getMeta_status());
		copyTo.setType(copyFrom.getType());
		copyTo.setExtra(copyFrom.getExtra());
		copyTo.setQuestion(copyFrom.getQuestion());
		copyTo.setSectionOrder(copyFrom.getSectionOrder());
		copyTo.setSectionIndex(copyFrom.getSectionIndex());
		copyTo.setSectionName(copyFrom.getSectionName());
	}
	
	public static void associateCITwithCT(CTGChecklistTemplate linkFrom, CTGChecklistItemTemplate linkTo) {
		linkTo.setClientRefIndex(linkFrom.getClient_index());
		linkTo.setTemplateRef(linkFrom.getId());
		linkTo.setByUser(linkFrom.getBy_user());
		linkTo.setClientUUID(linkFrom.getClient_uuid());
	}
	
	public static Result handleReorderRCIs(CTGRunChecklistItem from, CTGRunChecklistItem to, Context c) {
		//reorder, touch then save everything at and above the section order for the given 'to' item
		//set the 'from' item location to the new 'to' item location, touch then save it.
		Result r = new Result();
		CTGRunChecklistItemTable rciTable = TablePool.getRCITable(c);
		TableManager.acquireConnection(c, CTGConstants.DATABASE_NAME, rciTable);
		//if moving down the list, get the items after the current 'to' item
		ArrayList<CTGRunChecklistItem> list; 
		if (from.getSectionOrder() < to.getSectionOrder()) {
			//moving down the list - so we are inserting after the selected item.
			to.setSectionOrder(to.getSectionOrder()+1);
		} //else - for moving up, we just use the section order of the item we are moving to
		list = rciTable.getOtherItemsToReorder(from, to);
		//Just add one to each item in the list
		for (CTGRunChecklistItem rci : list) {
			rci.setSectionOrder(rci.getSectionOrder()+1);
			rci.update();
		}
		//now update the target item
		from.setSectionIndex(to.getSectionIndex());
		from.setSectionName(to.getSectionName());
		from.setSectionOrder(to.getSectionOrder());
		from.update();
		list.add(from);
		r = rciTable.insertOrUpdateArrayList(list);
		TableManager.releaseConnection(rciTable);
		return r;
	}
	
	public static Result handleReorderCITs(CTGChecklistItemTemplate from, CTGChecklistItemTemplate to, Context c) {
		//reorder, touch then save everything at and above the section order for the given 'to' item
		//set the 'from' item location to the new 'to' item location, touch then save it.
		Result r = new Result();
		CTGChecklistItemTemplateTable table = TablePool.getCITTable(c);
		TableManager.acquireConnection(c, CTGConstants.DATABASE_NAME, table);
		//if moving down the list, get the items after the current 'to' item
		ArrayList<CTGChecklistItemTemplate> list; 
		if (from.getSectionOrder() < to.getSectionOrder()) {
			//moving down the list - so we are inserting after the selected item.
			to.setSectionOrder(to.getSectionOrder()+1);
		} //else - for moving up, we just use the section order of the item we are moving to
		list = table.getOtherItemsToReorder(from, to);
		//Just add one to each item in the list
		for (CTGChecklistItemTemplate item : list) {
			item.setSectionOrder(item.getSectionOrder()+1);
			item.update();
		}
		//now update the target item
		from.setSectionIndex(to.getSectionIndex());
		from.setSectionName(to.getSectionName());
		from.setSectionOrder(to.getSectionOrder());
		from.update();
		list.add(from);
		r = table.insertOrUpdateArrayList(list);
		TableManager.releaseConnection(table);
		return r;
	}
		
	public static void wipeDatabase(Context context) {
		CTGRunChecklistTable rcTable = TablePool.getRCTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, rcTable);
		rcTable.wipeTable();
		TableManager.releaseConnection(rcTable);

		CTGTagTable ctgTable = TablePool.getTagTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, ctgTable);
		ctgTable.wipeTable();
		TableManager.releaseConnection(ctgTable);

		CTGChecklistTemplateTable ctTable = TablePool.getCTTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, ctTable);
		ctTable.wipeTable();
		TableManager.releaseConnection(ctTable);

		CTGChecklistItemTemplateTable citTable = TablePool.getCITTable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, citTable);
		citTable.wipeTable();
		TableManager.releaseConnection(citTable);

		CTGRunChecklistItemTable rciTable = TablePool.getRCITable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, rciTable);
		rciTable.wipeTable();
		TableManager.releaseConnection(rciTable);
		
		//clear sync notifications so data can be redownloaded.
		TableSyncDb syncDb = new TableSyncDb(context, CTGConstants.DATABASE_NAME);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, syncDb);
		syncDb.wipeTable();
		TableManager.releaseConnection(syncDb);

		//now notify the ListViews that the data may have been changed
		TagListNotifier.getNotifier().updateList();
		CTListNotifier.getNotifier().updateList();
		CITListNotifier.getNotifier().updateList();
		RCListNotifier.getNotifier().updateList();
		RCIListNotifier.getNotifier().updateList();
	}
}
