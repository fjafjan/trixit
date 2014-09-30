package com.trixit.framework;

import android.util.Log;

public class Vector2d {
	public double x, y;
	
	public Vector2d(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	// Returns this vector minus another vector
	public void minus(Vector2d otherVec){
		this.x -= otherVec.x;
		this.y -= otherVec.y;
	}
	
	public void plus(Vector2d otherVec){
		this.x += otherVec.x;
		this.y += otherVec.y;
	}

	public double length(){
		return (x*x) + (y*y);
	}

	public Vector2d add(Vector2d otherVec){
		return new Vector2d(this.x + otherVec.x, this.y + otherVec.y);
	}
	
	public Vector2d diff(Vector2d otherVec){
		return new Vector2d(this.x - otherVec.x, this.y - otherVec.y);
	}
	
	public void normalize(){
		double d = (x*x) + (y*y);
		this.x = x/d;
		this.y = y/d;
	}
	
	public void mult(double d){
		this.x *= d;
		this.y *= d;
	}
	
	public void divide(double d){
		this.x /= d;
		this.y /= d;
	}
	
	public void print(){
		Log.w("Debuggin", "x = " + this.x + ", y = " + this.y);
	}

	@Override
	public String toString(){
		return "x = " + this.x + ", y = " + this.y;
	}
}
