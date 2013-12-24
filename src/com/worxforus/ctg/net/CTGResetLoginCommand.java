package com.worxforus.ctg.net;

import junit.framework.Assert;
import android.content.Context;

import com.worxforus.Command;
import com.worxforus.Result;
import com.worxforus.ctg.CTGConstants;
import com.worxforus.db.TableManager;

public class CTGResetLoginCommand implements Command {

	Context c;
	private volatile int state= Command.STATE_WAITING;

	public CTGResetLoginCommand(Context c) {
		this.c = c;
	}
	
	@Override
	public synchronized Result execute() {
		state = Command.STATE_RUNNING;
		//Clear the sync db
		//do any other needed cleanup to reset for a different user
		Assert.assertNotNull(c);
			
		Result r = new Result();
		r = TableManager.resetSyncData(c, CTGConstants.DATABASE_NAME);
		state = Command.STATE_FINISHED;
		return r;
	}

	@Override
	public synchronized void attach(Context c) {
		this.c = c;
	}

	@Override
	public synchronized void release() {
		this.c = null;
	}

	@Override
	public void abort() {
		//Doesn't bother to do anything - no point in aborting this task
	}

	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return state;
	}

}
