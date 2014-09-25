package com.trixit.game;

import android.util.Log;

//import java.util.*;

public class Ball {
	private double xPos, yPos, xVel, yVel;
	
	
	double size, bounceCoef, weight, gravity;
	public Ball(double xPos,double yPos, double xVel, double yVel){
		this.xPos = xPos;
		this.yPos = yPos;
		this.xVel = xVel;
		this.yVel = yVel;
		size = 100; // I don't really make sure that this matches the size of the image right?
		bounceCoef = 0.7;
		weight = 1./10.;
		gravity = 0.5;
	}
	
	public double getX(){
		return xPos;
	}
	
	public double getY(){
		return yPos;
	}
	
	// Only updates the speeds based on the weight and the forces given
	
	/// TODO add the delta t here now we assume that the quadratic term is sufficiently small to ignore.
	public void updateForce(double forceX, double forceY){
		xVel += forceX/weight;
		yVel += (forceY/weight);
	}
	
	public void update(){
		Log.w("Debuggin", "Ball is at pos " + xPos + " " + yPos);
		xPos += xVel;
		yPos += yVel;
		yVel += gravity;
	}
	
	public void bounceX(double xPos){
		this.xPos = xPos;
		xVel *= -bounceCoef;
	}
	
	public void bounceY(double yPos){
		this.yPos = yPos;
		yVel *= -bounceCoef;
	}

}
