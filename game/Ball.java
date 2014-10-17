package com.trixit.game;

import java.util.ArrayList;

import android.util.Log;

import com.trixit.framework.Input.TouchEvent;
import com.trixit.framework.Vector2d;

public class Ball {
	private Vector2d pos;
	private Vector2d vel;
	private int unTouchedTime;
	
	private static double slowDown = 1;
	private static double gravity = 0.2;
	private static double minTouchTime = 25;
	
	public double size, bounceCoef, weight;
	public Ball(double xPos,double yPos, double xVel, double yVel){
		this.pos = new Vector2d(xPos, yPos);
		this.vel = new Vector2d(xVel, yVel);
		size = 100; // I don't really make sure that this matches the size of the image right?
		bounceCoef = 0.7;
		weight = 1./10.;
		unTouchedTime = 800;
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
		vel.plus(force.multret(slowDown/this.weight));
	}

	public void updateForce(double forceX, double forceY){
		vel.plus(new Vector2d(forceX/weight, forceY/weight).multret(slowDown));
	}
	
	public void update(double deltaTime){
		pos.plus( (vel.multret(deltaTime * slowDown)) );
		vel.y += gravity * deltaTime * slowDown;
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
	
	public double getSize(){
		return size;
	}
	
	public void setPos(Vector2d pos){
		this.pos = new Vector2d(pos);
	}

	public void setVel(Vector2d vel){
		this.vel = new Vector2d(vel);
	}

	public void setSlowDown(double slowdown){
		slowDown = slowdown;
	}
	
	public void setGravity(double g){
		gravity = g;
	}
	
	/// Collides this ball with another ball and performs the required velocity changes on both balls.
	public void collide(Ball otherBall){
		// Temporary debugging
		Vector2d posPre1 = new Vector2d(this.pos);
		Vector2d posPre2 = otherBall.getPos();
		Vector2d posPost1;
		Vector2d posPost2;
		
		
		// Okay so we want to find out the time it took since they actually intersected one another.
		Vector2d posDiff = this.pos.diff(otherBall.getPos());
		Vector2d velDiff = this.vel.diff(otherBall.getVel());
//		Log.w("Debuggin", "posDiff is  " + posDiff + " and vellDiff is " + velDiff);
		
		// Finds the two times when the balls will be intersecting
		double[] ts = findCollisionTime(posDiff, velDiff);
		double t1 = ts[0];
		double t2 = ts[1];
		
//		Log.w("Debuggin", "t1 is " + t1 + " t2 is " + t2);
 
		if((posDiff.abs() - (size*size))  > 0)
			Log.w("Debuggin", "!!!!!!!!!!!!!!!!!SOmething is messed up :/ !!!!!!!!!!!!!!!!!!!!!!!");

		// Okay so we have correctly found t. now we want to first virtually move the balls back to where
		// they should have collided. 		
//		Log.w("Debuggin", "The times that work are " + t1 + " and " + t2);
		pos = pos.add(vel.multret(t1));
		otherBall.setPos(otherBall.getPos().add(otherBall.getVel().multret(t1)));
		posDiff = this.pos.diff(otherBall.getPos());
		
		if(posDiff.hasNan()){
			Log.w("Debuggin", "PosDiff is");
			posDiff.print();
			Log.w("Debuggin", "VelDiff is");
			velDiff.print();
			Log.w("Debuggin", "Ball 1 pos is");
			pos.print();
			Log.w("Debuggin", "Ball 1 vel is");
			vel.print();
			Log.w("Debuggin", "Ball 2 pos is");
			otherBall.getPos().print();
			Log.w("Debuggin", "Ball 2 vel is");
			otherBall.getVel().print();			
			throw new RuntimeException();
		}
		// Debugging temporary stuff
		posPost1 = new Vector2d(this.pos);
		posPost2 = otherBall.getPos();
		
		// Debugging code for colission probelms. 
//		double moveDist1 = posPost1.diff(posPre1).length();
//		double moveDist2 = posPost2.diff(posPre2).length();
//		Log.w("Debuggin", "/n /n Posdiff is " + posDiff);
//		Log.w("Debuggin", "The balls were moved " + moveDist1 + " and " + moveDist2);
//		Log.w("Debuggin", "Our positions before were " + posPre1 + " and " + posPre2);
//		Log.w("Debuggin", "Our positions afterwards are " + posPost1 + " and " + posPost2 + pos + "");
		
		
		// We check if the current relative velocities will bring the balls further apart or not.
		if(areSameDirection(posDiff, velDiff)) {
			Log.w("Debuggin", "We think this is not a good colission to do ");
			return; // We don't perform a colission since they will separate naturally.
		}
		
		 	 
		// v' = v + atm constant mass factor * v1 - v2 dot x1 - x2 / r^2 * x1 - x2
		// http://en.wikipedia.org/wiki/Elastic_collision#Two-Dimensional_Collision_With_Two_Moving_Objects
		// posDiff = x1 - x2

		
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
		
//		Log.w("Debuggin", "frac is " + frac);
//		Log.w("Debuggin", "term1 squared is " + (term1*term1) + " and term 2 is " + term2);
		
		if( (term1 * term1) < term2){
			throw new RuntimeException("We are trying to find the colission time of two objects that have not yet collided."); 
		}
		double t1  = Math.sqrt((term1*term1) - term2) + term3;
		double t2  = Math.sqrt((term1*term1) - term2) - term3;
		t1 = -t1 / frac;
		t2 = t2 / frac;
		double[] ans = {t1, t2};
		return ans;
	}
	
	public boolean click(Vector2d clickPos, double forceConstant){
		if (unTouchedTime < minTouchTime)
			 return false;
		unTouchedTime = 0;
		
		Vector2d force = pos.diff(clickPos);
		force.normalize();
		force.y = -1;
		force.normalize();
		
		vel = force.multret(forceConstant * slowDown / weight);
//		Log.w("Debuggin", "We touch the ball, the resulting force is " + force.x + " " + force.y);
		return true;
	}
	
	/// Returns true if the ball has been clicked too recently. 
	
	/// Collides the ball with a swipe from user represented in a list of TouchEvents and an index
	/// i where the swipe first collided with the ball. deltaTime is the total duration of the swipe.
	/// Returns if the ball was actually moved or if it was too recent.
	public boolean drag(ArrayList<TouchEvent> events, int i, double deltaTime){
		if( unTouchedTime < minTouchTime ){
			Log.w("Debuggin", "We choose to not detect this touch.  ." + vel);
			return false;
		}
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
//		touchDir.mult(size * 2);
		updateForce(touchDir.multret(-dragVel.length()/15));
//		Vector2d vBallPos = pos.add(touchDir);
//		Ball virtualBall = new Ball(vBallPos, dragVel);
//		Log.w("Debuggin", "Before colissions vel is ." + vel);
//		this.collide(virtualBall);
//		Log.w("Debuggin", "after colissions vel is ." + vel);
		// Remove the virtual ball.
//		virtualBall = null;
		unTouchedTime = 0;
		return true;		
	}
}
