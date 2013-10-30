package com.worxforus.app;

import java.util.Observable;

import com.worxforus.Utils;

import android.util.Log;

/*
 * RunChecklist List Notifier - This is used to provide an interface for any RunChecklist List Views so they can be updated
 * on the fly.  This is an Observable holder - singleton style
 * 
 * Usage:
 * RCListNotifier.getNotifier().updateList();
 * 
 * Initial setup in remote fragment/class:
 * RCListNotifier.getNotifier().addObserver(this);
 */

public class RCListNotifier extends Observable {

	private static RCListNotifier instance = new RCListNotifier();

	//mark to reload entire list when this function is called
	public void updateList() {
		Utils.LogD(this.getClass().getName(), "Alerted that data was updated.");
		setChanged();
		notifyObservers();
	}
	
	public RCListNotifier() {
		Utils.LogD(this.getClass().getName(), "Notifier was created.");
	}

	public static RCListNotifier getNotifier() {
		return instance;
	}

}
