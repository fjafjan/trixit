package com.trixit.game;

import com.trixit.framework.Vector2d;
import com.trixit.game.GameScreen.GameState;

import android.util.Log;

//import java.util.*;

public class Ball {
	private double xPos, yPos, xVel, yVel;
	private Vector2d pos, vel;
	
	public double size, bounceCoef, weight, gravity;
	public Ball(double xPos,double yPos, double xVel, double yVel){
		this.xPos = xPos;
		this.yPos = yPos;
		this.xVel = xVel;
		this.yVel = yVel;
		this.pos = new Vector2d(xPos, yPos);
		this.vel = new Vector2d(xVel, yVel);
		size = 100; // I don't really make sure that this matches the size of the image right?
		bounceCoef = 0.7;
		weight = 1./20.;
		gravity = 0;
	}
	
	public Ball(Vector2d pos, Vector2d vel){
		this(pos.x, pos.y, vel.x, vel.y);
	}
	
	public double getX(){
		return xPos;
	}
	
	public double getY(){
		return yPos;
	}
	
	// Only updates the speeds based on the weight and the forces given
	
	/// TODO add the delta t here now we assume that the quadratic term is sufficiently small to ignore.
	public void updateForce(Vector2d force){
		xVel += force.x/weight;
		yVel += force.y/weight;
	}

	
	
	public void updateForce(double forceX, double forceY){
		xVel += forceX/weight;
		yVel += (forceY/weight);
	}
	
	public void update(double deltaTime){
		xPos += xVel * deltaTime;
		yPos += yVel * deltaTime;
		yVel += gravity * deltaTime;
	}
	
	public void bounceX(double xPos){
		this.xPos = xPos;
		xVel *= -bounceCoef;
	}
	
	public void bounceY(double yPos){
		this.yPos = yPos;
		yVel *= -bounceCoef*0.7;
	}
	
	public Vector2d getPos(){
		pos = new Vector2d(this.xPos, this.yPos);
		return pos;
	}

	public Vector2d getVel(){
		vel = new Vector2d(this.xVel, this.yVel);
		return vel;
	}

	public void collide(Ball otherBall){
		// v' = v + atm constant mass factor * v1 - v2 dot x1 - x2 / r^2 * x1 - x2
		double xPosDiff = xPos - otherBall.xPos;
		double xVelDiff = xVel - otherBall.xVel;
		
		double yPosDiff = yPos - otherBall.yPos;
		double yVelDiff = yVel - otherBall.yVel;
		
		double innerX = (xVelDiff) * (xPosDiff);
		double innerY = (yVelDiff) * (yPosDiff);
		double innerTot = innerX + innerY;
		double dist = (xPosDiff * xPosDiff ) + (yPosDiff * yPosDiff); 
		double massFactor = 1.;
		// The size factor is weird but lets see how it works at least
		xVel -= massFactor * innerTot * xPosDiff / dist;
		yVel -= massFactor * innerTot * yPosDiff / dist;
		otherBall.xVel = otherBall.xVel  + massFactor * innerTot * xPosDiff / dist;
		otherBall.yVel = otherBall.yVel  + massFactor * innerTot * yPosDiff / dist;
	}
}
