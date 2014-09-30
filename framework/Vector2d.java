package com.trixit.framework;

import android.util.Log;
/// This is a class meant to represent vectors in two dimensional space. Has operations for most of the simple algebraic 
/// operations I could think of, but I am sure I missed a bunch. 
public class Vector2d {
	public double x, y;
	
	/// Simple constructor, creates a vector with components x and y.
	public Vector2d(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	/// Copy constructors, copies the vector otherVec. 
	public Vector2d(Vector2d otherVec){
		this.x = otherVec.x;
		this.y = otherVec.y;
	}
	
	// Subtracts otherVec from this vector. 
	public void minus(Vector2d otherVec){
		this.x -= otherVec.x;
		this.y -= otherVec.y;
	}
	
	public void plus(Vector2d otherVec){
		this.x += otherVec.x;
		this.y += otherVec.y;
	}
	public void mult(double d){
		this.x *= d;
		this.y *= d;
	}
	
	public Vector2d multret(double d){
		return new Vector2d(x * d, y * d);
	}
	
	
	public void divide(double d){
		this.x /= d;
		this.y /= d;
	}

	public Vector2d add(Vector2d otherVec){
		return new Vector2d(this.x + otherVec.x, this.y + otherVec.y);
	}

	public double length(){
		return Math.sqrt((x*x) + (y*y));
	}

	/// Does the same thing as diff, but I think it is often a more descriptive name, as it's the vector from
	/// one position to another? It's just minus between vectors...
	public Vector2d to(Vector2d otherVec){
		return new Vector2d(this.x - otherVec.x, this.y - otherVec.y);
	}

	
	public Vector2d diff(Vector2d otherVec){
		return new Vector2d(this.x - otherVec.x, this.y - otherVec.y);
	}
	
	public void normalize(){
		double d = (x*x) + (y*y);
		this.x = x/d;
		this.y = y/d;
	}
	
	public Vector2d dot(Vector2d otherVec){
		return new Vector2d(this.x * otherVec.x, this.y * otherVec.y);
	}

	public double innerProd(Vector2d otherVec){
		return (this.x * otherVec.x) +  (this.y * otherVec.y);
	}
	
	public void print(){
		Log.w("Debuggin", "x = " + this.x + ", y = " + this.y);
	}

	@Override
	public String toString(){
		return "x = " + this.x + ", y = " + this.y;
	}
}
