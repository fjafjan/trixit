package com.trixit.game;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.trixit.framework.Input.TouchEvent;


public class DragEvent {
	
	List<TouchEvent> touchEvents;
	int id;
	
	public DragEvent(TouchEvent e){
		this.id = e.pointer;
		touchEvents = new ArrayList<TouchEvent>();
		touchEvents.add(e);
	}
	
	public void addEvent(TouchEvent e){
		touchEvents.add(e);
	}
	
	public void printEvents(){
		for (int i = 0; i < touchEvents.size(); i++) {
			Log.w("Debuggin", "pos x=" + touchEvents.get(i).x + ", y = " + touchEvents.get(i).y);
		}
	}
}
