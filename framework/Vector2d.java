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

	public void plus(Vector2d otherVec){
		this.x += otherVec.x;
		this.y += otherVec.y;
	}

	public void plus(double d){
		this.x += d;
		this.y += d;
	}
	
	
	// Subtracts otherVec from this vector. 
	public void minus(Vector2d otherVec){
		this.x -= otherVec.x;
		this.y -= otherVec.y;
	}
	
	
	public void mult(double d){
		this.x *= d;
		this.y *= d;
	}
	
	public Vector2d multret(double d){
		return new Vector2d(x * d, y * d);
	}
	
	public Vector2d multPoint(Vector2d otherVec){
		return new Vector2d(x * otherVec.x, y * otherVec.y);
	}
	
	public void divide(double d){
		this.x /= d;
		this.y /= d;
	}

	public Vector2d add(Vector2d otherVec){
		return new Vector2d(this.x + otherVec.x, this.y + otherVec.y);
	}

	// Returns the euclidian length of the vector
	public double length(){
		return Math.sqrt((x*x) + (y*y));
	}

	// Returns the squared length. 
	public double abs(){
		return (x*x) + (y*y);
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
		double d = this.length();
		this.x /= d;
		this.y /= d;
	}
	
	public Vector2d norm(){
		double d = this.length();
		return new Vector2d(this.x/d, this.y/d);
	}
	
	// return scalar dot product
	public double dot(Vector2d otherVec){
		return (this.x * otherVec.x) +  (this.y * otherVec.y);
	}
	
	// returns the vector dot product.
	public Vector2d prod(Vector2d otherVec){
		return new Vector2d(this.x * otherVec.x, this.y * otherVec.y);
	}

	/// Returns the cross product, this x otherVec.
	public double cross(Vector2d otherVec){
		return (this.x * otherVec.y) - (this.y * otherVec.x);
	}
	
	public double innerProd(Vector2d otherVec){
		return (this.x * otherVec.x) +  (this.y * otherVec.y);
	}
	
	// Project this vector on the other vector.
	public Vector2d proj(Vector2d otherVec){
		double dotProd = this.dot(otherVec.norm());
		return otherVec.norm().multret(dotProd);
	}
	// Simply returns the sum of x and y.
	public double sum(){
		return this.x + this.y;
	}
	
	/// returns if either of the two elements are NaN
	public boolean hasNan(){
		return (this.x == Double.NaN) || (this.y == Double.NaN);
	}
	
	public void print(){
		Log.w("Debuggin", "x = " + this.x + ", y = " + this.y);
	}

	
	@Override
	public String toString(){
		return "x = " + this.x + ", y = " + this.y;
	}
}
