package com.worxforus.app;

import java.util.Observable;

import com.worxforus.Utils;

import android.util.Log;

/*
 * CTGRunChecklistItem List Notifier - This is used to provide an interface for any CTGRunChecklistItem List Views so they can be updated
 * on the fly.  This is an Observable holder - singleton style
 * 
 * Usage:
 * RCIListNotifier.getNotifier().updateList();
 * 
 * Initial setup in remote fragment/class:
 * RCIListNotifier.getNotifier().addObserver(this);
 */


public class RCIListNotifier extends Observable {

	private static RCIListNotifier instance = new RCIListNotifier();

	//mark to reload entire list when this function is called
	public void updateList() {
		Utils.LogD(this.getClass().getName(), "Alerted that data was updated.");
		setChanged();
		notifyObservers();
	}
	
	public RCIListNotifier() {
		Utils.LogD(this.getClass().getName(), "Notifier was created.");
	}

	public static RCIListNotifier getNotifier() {
		return instance;
	}

}
