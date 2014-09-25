package com.trixit.game;

import android.util.Log;

//import java.util.*;

public class Ball {
	private double xPos, yPos, xVel, yVel;
	
	
	public double size, bounceCoef, weight, gravity;
	public Ball(double xPos,double yPos, double xVel, double yVel){
		this.xPos = xPos;
		this.yPos = yPos;
		this.xVel = xVel;
		this.yVel = yVel;
		size = 100; // I don't really make sure that this matches the size of the image right?
		bounceCoef = 0.7;
		weight = 1./20.;
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

	public void collide(Ball otherBall){
		// v' = v + atm constant mass factor * v1 - v2 dot x1 - x2 / r^2 * x1 - x2
		double xPosDiff = xPos - otherBall.xPos;
		double xVelDiff = xVel - otherBall.xVel;
		double yPosDiff = yPos - otherBall.yPos;
		double yVelDiff = yVel - otherBall.yVel;
		
		double innerX = (xVelDiff) * (xPosDiff);
		double innerY = (yVelDiff) * (yPosDiff);
		
		double massFactor = 1.;
		// The size factor is weird but lets see how it works at least
		xVel += massFactor * innerX * xPosDiff / (size*size);
		yVel += massFactor * innerY * yPosDiff / (size*size);
		// This aint ez. 
	}
}
