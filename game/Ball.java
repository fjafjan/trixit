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
	
	public void setPos(Vector2d otherVec){
		this.pos = new Vector2d(otherVec);
	}


	/// Collides this ball with another ball and performs the required velocity changes on both balls.
	/// TODO implement predictive collisions to avoid balls getting stuck in one another, 
	/// weird looking bounces and so forth.
	public void collide(Ball otherBall){
		// v' = v + atm constant mass factor * v1 - v2 dot x1 - x2 / r^2 * x1 - x2
		// http://en.wikipedia.org/wiki/Elastic_collision#Two-Dimensional_Collision_With_Two_Moving_Objects
		// posDiff = x1 - x2
		
		// Okay so we want to find out the time it took since they actually intersected one another.
		
		
		Vector2d posDiff = this.pos.diff(otherBall.getPos());
		Vector2d posDiff2 = posDiff.multret(-1);
		// velDiff = x1 - x2
		Vector2d velDiff = this.vel.diff(otherBall.getVel());
		Vector2d velDiff2 = velDiff.multret(-1);

		// We just want D to be one diameter. 
		// V - v
		/// t = (-sqrt(-+2 k x+2 l y)/
		
		// (-2 k x-2 l y)^2
		double[] ts = findCollisionTime(posDiff, velDiff);
		double t1 = ts[0];
		double t2 = ts[1];


 
		if((posDiff.abs() - (size*size))  > 0)
			Log.w("Debuggin", "!!!!!!!!!!!!!!!!!SOmething is messed up :/ !!!!!!!!!!!!!!!!!!!!!!!");

		// Okay so we have correctly found t. now we want to first virtually move the balls back to where
		// they should have collided. 
		
		double postDist = posDiff.add(velDiff.multret(t1)).length();
		
		/// Now we want to make sure that it is a "correct" colission where the current velocities are 
		/// bringing the balls together. 
		
		
		Log.w("Debuggin", "The times that work are " + t1 + " and " + t2);
		pos.add(vel.multret(t1));
		otherBall.setPos(otherBall.getPos().add(otherBall.getVel().multret(t1)));
		posDiff = this.pos.diff(otherBall.getPos());
			

		/// There is a better way of checking this using normalized vectors I am sure.
		
		// I think we simply check if the projection of the velDiff on the posVEl vector  
		/// If the distance between the balls is larger after a short time span, then 
		if(areSameDirection(posDiff, velDiff)) {
			Log.w("Debuggin", "We think this is not a good colission to do ");
			return; // We don't perform a colission since they will separate naturally.
		}
			 

		// Make sure that it has indeed worked.
//		double testDist = this.pos.diff(otherBall.getPos()).length();
		
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
	
	
	private boolean areSameDirection(Vector2d vec1, Vector2d vec2){
		return (vec1.dot(vec2) >= 0);
	}
	
	private double[] findCollisionTime(Vector2d posDiff, Vector2d velDiff) {
		double term1 = 2 * posDiff.multPoint(velDiff).sum();

		//  4 (k^2+l^2) (-D^2+x^2+y^2))
		double term2 = 4 * velDiff.abs() * (posDiff.abs() - (size*size));
		
		// +2 k x+2 l y)
		double term3 = 2 * (posDiff.multPoint(velDiff)).sum();
		
		// (2 (k^2+l^2))
		double frac = 2 * velDiff.abs();
		
		double t1  = Math.sqrt((term1*term1) - term2) + term3;
		double t2  = Math.sqrt((term1*term1) - term2) - term3;
		t1 = -t1 / frac;
		t2 = t2 / frac;
		double[] ans = {t1, t2};
		return ans;
	}

	public double testTimeUpdate(double time, Ball otherBall){
		Vector2d backupPos = new Vector2d(pos);
		Vector2d otherPos = otherBall.getPos();
		backupPos = backupPos.add(vel.multret(time));
		otherPos = otherPos.add(otherBall.getVel().multret(time));
		return backupPos.diff(otherPos).length();
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
