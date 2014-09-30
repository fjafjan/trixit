package com.trixit.game;

import java.util.ArrayList;

import android.util.Log;

import com.trixit.framework.Input.TouchEvent;


public class DragEvent {
	
	ArrayList<TouchEvent> touchEvents;
	int id;
	ArrayList<Integer> collidedWith;
	
	public DragEvent(TouchEvent e){
		this.id = e.pointer;
		this.collidedWith = new ArrayList<Integer>();
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
	
	public ArrayList<TouchEvent> getEvents(){
		return touchEvents;
	}
	
	public void collided(int ballIndex){
		collidedWith.add(ballIndex);
	}
	
	public void clearCollisions(){
		collidedWith.clear();
	}
}
