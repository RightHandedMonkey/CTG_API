package com.worxforus.app;

import java.util.Observable;

import com.worxforus.Utils;

import android.util.Log;

/*
 * CTGChecklistItemTemplate List Notifier - This is used to provide an interface for any CTGChecklistItemTemplate List Views so they can be updated
 * on the fly.  This is an Observable holder - singleton style
 * 
 * Usage:
 * CITListNotifier.getNotifier().updateList();
 * 
 * Initial setup in remote fragment/class:
 * CITListNotifier.getNotifier().addObserver(this);
 */


public class CITListNotifier extends Observable {

	private static CITListNotifier instance = new CITListNotifier();

	//mark to reload entire list when this function is called
	public void updateList() {
		Utils.LogD(this.getClass().getName(), "Alerted that data was updated.");
		setChanged();
		notifyObservers();
	}
	
	public CITListNotifier() {
		Utils.LogD(this.getClass().getName(), "Notifier was created.");
	}

	public static CITListNotifier getNotifier() {
		return instance;
	}

}
