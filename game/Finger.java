package com.trixit.game;

import java.util.ArrayList;

import android.util.Log;

import com.trixit.framework.Input.TouchEvent;
import com.trixit.framework.Vector2d;

public class Finger {
	/// What should this class exactly do?
	/// We want this to represent a finger. 
	/// If it is too new it should be a click. if it is moving too slowly
	/// We update the fingers each update and keep a list of fingers isntead of a list of touchEvents
	/// we then want to modify the checking function to use fingers instead of touch events and drag events. 
	/// I think this generalizes and simplifies in general. 
	/// I think this should really be done in the input class, where we keep a log of old and new touches

	static double maxVel = 2;			/// The maximum allowed finger velocity 
	
	int id;								/// The ID of this finger.
	int memoryLength = 100;				/// The amount of time which we remember old events.
	public Vector2d pos;				/// The current perceived location of this finger. 
	public Vector2d vel;				/// The current perceived velocity of this finger. 
	ArrayList<TouchEvent> newEvents;	/// The new events of this finger since the last iteration.
	ArrayList<TouchEvent> oldEvents;	/// The events that have happened
	ArrayList<Double> eventTimes;		/// The times that these events occured
	public boolean destroy = false;		/// If we should destroy this finger
	
	
	ArrayList<Vector2d> oldVels;		/// The velocities this finger has had in the past.
	int nVelAvg = 2;
	
	public Finger(TouchEvent touch){
		this.id = touch.pointer;
		this.pos = new Vector2d(touch.x, touch.y);
		this.vel = new Vector2d(0,0);
		
		/// initialize  
		newEvents = new ArrayList<TouchEvent>();
		oldEvents = new ArrayList<TouchEvent>();
		eventTimes = new ArrayList<Double>();
		
		/// maybe tmp
		oldVels = new ArrayList<Vector2d>();
	}
	
	
	/// Add a detected touch event assosiated with this Finger.
	public void addEvent(TouchEvent e){
		/// If it is indeed for the correct finger.
		if(e.pointer != this.id){
			throw new RuntimeException("Incorrect touchEvent associated wtih this finger");
		}
		/// We need to manually make a copy of the touch event since touchEvent has no copy function.
		TouchEvent copy = new TouchEvent();
		copy.x = e.x;
		copy.y = e.y;
		copy.type = e.type;
		copy.pointer = e.pointer;
		this.newEvents.add(copy);
	}
	
	
	 
	public void updateFinger(double deltaTime){
		/// First just sanity check that eventTimes is as long as oldEvents
		if(oldEvents.size() != eventTimes.size()){
			throw new RuntimeException("Not matching lengths of oldEvents and eventTimes, oldLen = " + 
					 oldEvents.size() + " timeLen = " + eventTimes.size());
		}
		
		/// Update the old times. Since we can only make an arrayList with wrapper
		/// class Doubles, we use set instead of just +=. 
		for(int i = 0 ; i < eventTimes.size() ; i++){
			double newTime  = eventTimes.get(i) + deltaTime;
			eventTimes.set(i, newTime);
		}
				
		// Add all new events and a corresponding 0 time since they happened now.
		for(int i = 0 ; i < newEvents.size() ; i++){
			oldEvents.add(newEvents.get(i));
			eventTimes.add(Double.valueOf(0)); /// a
		}
		
		
		// Remove the old events that are older than memory.
		pruneOldEvents();
		
		// check for position and velocity among the events we have in recent memory.
		if( !oldEvents.isEmpty() ){
			updatePosition();
			updateVelocity();			
		}

		// Clean out the newEvents array.
		newEvents.clear(); // No longer new these events.
	}

	
	// The idea here is pretty simple. Since new events are appended at the back, and all are
	// increased the same amount, we get a sorted array with decreasing value. Thus if we 
	// want to remove all entries larger than our memoryLength, we just find the first value
	// that is smaller, and keep that and all entries after it. 
	/// If we can and need to do some pruning, ie the longest time is longer than memory.	
	private void pruneOldEvents(){
		if( !eventTimes.isEmpty() && eventTimes.get(0) > memoryLength){
			// We want to keep everything after some cutoff.
			int cutOff = 0;
			
			for(int i = 0 ; i < eventTimes.size() ; i++){
				// The first value where it's smaller we stop removing. 
				if(eventTimes.get(i) <= memoryLength){
					cutOff = i;
					break;
				}
			}
			
			eventTimes = new ArrayList<Double>( eventTimes.subList(cutOff, eventTimes.size()) );
			oldEvents  = new ArrayList<TouchEvent>( oldEvents.subList(cutOff, oldEvents.size()) );
			
		}

	}
	
	private void updatePosition(){
		// The current position of this finger is the most recent detected position
		TouchEvent newest = oldEvents.get(oldEvents.size() - 1);
		this.pos = new Vector2d(newest.x, newest.y);
	}
	
	/// Updates our velocity based on the past positions of fingers. 
	private void updateVelocity(){
		// The current velocity is the vector from our oldest position to our current position. 
		// this is however not normalised, so we do that below. 

		TouchEvent oldest = oldEvents.get(0);
		this.vel = this.pos.diff(new Vector2d(oldest.x, oldest.y));
		//this.vel.divide(deltaTime + eventTimes.get(0));
		
		normalizeVelocity();
	
		/// Old events include the latest position, which means if we only have 1 position
		/// We have no velocity. 
		if(oldEvents.size() == 1){
			this.vel = new Vector2d(0,0);
		}else{
			this.vel.divide(oldEvents.size()-1);
		}
		
		
		///
		computeAverageVelocity();
		
		Vector2d averageVel = new Vector2d(0,0);

		oldVels.add(new Vector2d(this.vel));
		if(oldVels.size() > nVelAvg){
			oldVels.remove(0);
		}
		for (int i = 0; i < oldVels.size(); i++) {
			averageVel = averageVel.add(oldVels.get(i));
		}
		averageVel.mult(1./(10*oldVels.size()));
		if(averageVel.length() > 0){
			averageVel.divide( Math.sqrt(averageVel.length()) );
		}
//		Log.w("Debuggin", "Current finger velocity is " + this.vel.length() + " based on " + oldEvents.size());

		this.vel = averageVel;
		/// TMP DEBUG STUFF
//		Vector2d old = new Vector2d(oldest.x, oldest.y);
//		Log.w("Debuggin", "Average finger velocity is " + averageVel.length() + " based on " + oldVels.size());
		
	}
	
	public void printPositions(){
		for (int i = 0; i < oldEvents.size(); i++) {
			Log.w("Debuggin", "Position " + i + " is at " + oldEvents.get(i).x  + " , " + oldEvents.get(i).y);
		}
	}
	
	private void computeAverageVelocity(){
		Vector2d averageVel = new Vector2d(0,0);

		oldVels.add(new Vector2d(this.vel));
		if(oldVels.size() > nVelAvg){
			oldVels.remove(0);
		}
		for (int i = 0; i < oldVels.size(); i++) {
			averageVel = averageVel.add(oldVels.get(i));
		}
		averageVel.mult(1./(10*oldVels.size()));
		if(averageVel.length() > 0){
			averageVel.divide( Math.sqrt(averageVel.length()) );
		}
		
	}
	
	
	
	private void normalizeVelocity(){
		
	}
}
