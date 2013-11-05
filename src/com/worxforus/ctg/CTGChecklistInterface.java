package com.worxforus.ctg;

import java.util.ArrayList;

import junit.framework.Assert;
import android.content.Context;

import com.worxforus.Result;
import com.worxforus.ctg.db.CTGChecklistItemTemplateTable;
import com.worxforus.ctg.db.CTGRunChecklistItemTable;
import com.worxforus.ctg.db.CTGRunChecklistTable;
import com.worxforus.db.TableManager;

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
	 * Returns: Result.object - is a reference to the actual created CTGRunChecklist object
	 * @param template
	 * @param uuid
	 * @param context
	 * @return result
	 */
	public static Result createRunChecklistFromTemplate(CTGChecklistTemplate template, String uuid, Context context) {
		Result result;
		CTGRunChecklist rc = new CTGRunChecklist();
		rc.setTitle(template.getTitle());
		rc.setTemplateRef(template.getId());
		rc.setClientRefIndex(template.getClient_index());
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
			//fill in data from template
			CTGChecklistInterface.copyCITtoRCI(cit, rci);
			//associate with checklist
			rci.setRunChecklistRef(rc.getId());
			rci.setClientRunChecklistRefIndex(rc.getClientIndex());
			rci.setClientUUID(rc.getClientUUID());
			rciItems.add(rci);
		}

		//now create all objects in db
		CTGRunChecklistItemTable rciTable = TablePool.getRCITable(context);
		TableManager.acquireConnection(context, CTGConstants.DATABASE_NAME, rciTable);
		result = rciTable.createLocalRunChecklistItems(rciItems);
		TableManager.releaseConnection(rciTable);
		return result;
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
}
