package com.worxforus.app;

import java.util.Observable;

import com.worxforus.Utils;
import com.worxforus.ctg.CTGTag;

import android.util.Log;

/*
 * CTGTag List Notifier - This is used to provide an interface for any CTGTag List Views so they can be updated
 * on the fly.  This is an Observable holder - singleton style
 * 
 * Usage:
 * TagListNotifier.getNotifier().updateList();
 * 
 * Initial setup in remote fragment/class:
 * TagListNotifier.getNotifier().addObserver(this);
 */


public class TagListNotifier extends Observable {

	private static TagListNotifier instance = new TagListNotifier();

	//mark to reload entire list when this function is called
	public void updateList() {
		Utils.LogD(this.getClass().getName(), "Alerted that data was updated.");
		setChanged();
		notifyObservers();
	}
	
	public TagListNotifier() {
		Utils.LogD(this.getClass().getName(), "Notifier was created.");
	}

	public static TagListNotifier getNotifier() {
		return instance;
	}

}
