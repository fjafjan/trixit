package com.trixit.game;

import java.util.ArrayList;

import android.util.Log;

import com.trixit.framework.Input.TouchEvent;
import com.trixit.framework.Vector2d;

public class Ball {
//	private double xPos, yPos, xVel, yVel;
	private Vector2d pos;
	private Vector2d vel;
	private int unTouchedTime;
	
	public double size, bounceCoef, weight, gravity;
	public Ball(double xPos,double yPos, double xVel, double yVel){
		this.pos = new Vector2d(xPos, yPos);
		this.vel = new Vector2d(xVel, yVel);
		size = 100; // I don't really make sure that this matches the size of the image right?
		bounceCoef = 0.7;
		weight = 1./20.;
		gravity = 0;
		unTouchedTime = 1000;
	}
	
	public Ball(Vector2d pos, Vector2d vel){
		this(pos.x, pos.y, vel.x, vel.y);
	}
	
	public double getX(){
		return pos.x;
	}
	
	public double getY(){
		return pos.y;
	}
	
	// Only updates the speeds based on the weight and the forces given
	
	/// TODO add the delta t here now we assume that the quadratic term is sufficiently small to ignore.
	public void updateForce(Vector2d force){
		vel.plus(force.multret(1./this.weight));
	}

	public void updateForce(double forceX, double forceY){
		vel.plus(new Vector2d(forceX/weight, forceY/weight));
	}
	
	public void update(double deltaTime){
		pos.plus( (vel.multret(deltaTime)) );
		vel.y += gravity * deltaTime;
		unTouchedTime += deltaTime;
//		Log.w("Debuggin", "unTouchedTime for this update is " + unTouchedTime);
	}
	
	public void bounceX(double xPos){
		this.pos.x = xPos;
		this.vel.x *= -bounceCoef;
	}
	
	public void bounceY(double yPos){
		this.pos.y  = yPos;
		this.vel.y *= -bounceCoef*0.7;
	}
	
	// These functions return a copy vector in order to avoid another routine tampering 
	// with these values in unexpected ways.
	public Vector2d getPos(){
		return new Vector2d(pos);
	}

	public Vector2d getVel(){
	    
		return new Vector2d(vel);
	}	

	/// Collides this ball with another ball and performs the required velocity changes on both balls.
	/// TODO implement predictive collisions to avoid balls getting stuck in one another, 
	/// weird looking bounces and so forth.
	public void collide(Ball otherBall){
		// v' = v + atm constant mass factor * v1 - v2 dot x1 - x2 / r^2 * x1 - x2
		// http://en.wikipedia.org/wiki/Elastic_collision#Two-Dimensional_Collision_With_Two_Moving_Objects
		// posDiff = x1 - x2
		Vector2d posDiff = this.pos.diff(otherBall.getPos());
		// velDiff = x1 - x2
		Vector2d velDiff = this.vel.diff(otherBall.getVel());
		
		// innerProd =  < x1 - x2, v1 - v2 > 
		// massFactor = 2 * m2 / (m1 + m2)
		// dist = || x1 - x2 || ^ 2
		
		double innerProd = posDiff.innerProd(velDiff);
		
		// keep this as 1 atm. 
		// otherBall.weight*2. / (this.weight + otherBall.weight);
		double massFactor = 1 ;  
		
		double dist = posDiff.length() * posDiff.length();
		
		Vector2d newForce = posDiff.multret(massFactor * innerProd / dist); 
		
		vel.minus(newForce);
		otherBall.updateForce(newForce.multret(weight));		
	}
	
	/// Collides the ball with a swipe from user represented in a list of TouchEvents and an index
	/// i where the swipe first collided with the ball. deltaTime is the total duration of the swipe.
	/// Returns if the ball was actually moved or if it was too recent.
	public boolean drag(ArrayList<TouchEvent> events, int i, double deltaTime){
		if( unTouchedTime < 40 )
			return false;
		// We haven\t recently been touched, so we proceed with touching.
		// The position of impact
		Vector2d touchPos = new Vector2d(events.get(i).x,events.get(i).y);
		// The starting point of this drag/swipe
		Vector2d initalPos = new Vector2d(events.get(0).x,events.get(0).y);
		Vector2d endPos = new Vector2d(events.get(events.size()-1).x,events.get(events.size()-1).y);
		// The direction of the swipe is assumed to be straight and linear.
		Vector2d dragVel = endPos.diff(initalPos);
		dragVel.divide(deltaTime);
		
		// The vector from the ball and the touchPoint
		Vector2d touchDir = touchPos.diff(pos);
		touchDir.normalize();
		
		// Create a virtual ball that is adjacent to this ball in direction
		// of the touch.
		touchDir.mult(size * 2);
		Vector2d vBallPos = pos.add(touchDir);
		Ball virtualBall = new Ball(vBallPos, dragVel);
		
		this.collide(virtualBall);
		// Remove the virtual ball.
		virtualBall = null;
		unTouchedTime = 0;
		return true;		
	}
}
