package com.worxforus.app;

import java.util.Observable;

import com.worxforus.Utils;

import android.util.Log;

/*
 * ChecklistTemplate List Notifier - This is used to provide an interface for any ChecklistTemplate List Views so they can be updated
 * on the fly.  This is an Observable holder - singleton style
 * 
 * Usage:
 * CTListNotifier.getNotifier().updateList();
 * 
 * Initial setup in remote fragment/class:
 * CTListNotifier.getNotifier().addObserver(this);
 */


public class CTListNotifier extends Observable {

	private static CTListNotifier instance = new CTListNotifier();

	//mark to reload entire list when this function is called
	public void updateList() {
		Utils.LogD(this.getClass().getName(), "Alerted that data was updated.");
		setChanged();
		notifyObservers();
	}
	
	public CTListNotifier() {
		Utils.LogD(this.getClass().getName(), "Notifier was created.");
	}

	public static CTListNotifier getNotifier() {
		return instance;
	}

}
