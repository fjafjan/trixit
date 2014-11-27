package com.trixit.game;

import java.util.ArrayList;

import android.util.Log;

import com.trixit.framework.Input.TouchEvent;
import com.trixit.framework.Vector2d;

public class Ball {
	private Vector2d pos;
	private Vector2d vel;
	private double angle, spin, maxSpin;
	private int unTouchedTime;
	
	private static double gravity = 0.2;
	private static double minTouchTime = 25;
	private static double momentOfInertia = 0.2;
	private static double friction = 0.5;
	private static double clickSpin = 3;
	private static double maxSpeed = 4;
	
	public double size, bounceCoef, weight;
	
	private double DegreesToRadians = 2 * 3.14159265 / 360.;
	private double RadiansToDegrees = 1./DegreesToRadians;
	
	public Ball(double xPos,double yPos, double xVel, double yVel, double spin){
		this.pos = new Vector2d(xPos, yPos);
		this.vel = new Vector2d(xVel, yVel);
		this.spin = spin;
		size = 100; // I don't really make sure that this matches the size of the image right?
		bounceCoef = 0.7;
		weight = 1./10.;
		unTouchedTime = 0;
		angle = 0;
		maxSpin = 40;
	}
	
	
	public Ball(Vector2d pos, Vector2d vel){
		this(pos.x, pos.y, vel.x, vel.y);
	}

	public Ball(double xPos, double yPos, double xVel, double yVel){
		this(xPos, yPos, xVel, yVel, 0);
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
		vel.plus(force.multret(1/this.weight));
	}

	public void updateForce(double forceX, double forceY){
		vel.plus(new Vector2d(forceX/weight, forceY/weight));
	}
	
	/// Updates the position and angle (rotation) of this ball. 
	public void update(double deltaTime){
		pos.plus( (vel.multret(deltaTime)) );
		vel.y += gravity * deltaTime;
		if(spin > maxSpin){
			spin = maxSpin;
		}
		this.angle += spin * deltaTime;
		unTouchedTime += deltaTime;
	}
	
	/// Bounces along the X axis.
	public void bounceX(double xPos, int side){
		this.pos.x = xPos;
		this.vel.x *= -bounceCoef;
		
		/// We compute the relative velocity of the edge of the ball and the wall and
		/// based on the amount of friction this will result in a force that pushes
		/// the ball in a vertical direction, and reduces or increases the spin. 
		double relativeVelocity = vel.y + (side * spin); 
		double spinFactor = side * ( relativeVelocity * friction );
		this.spin -= spinFactor;

		this.vel.y += size * -spinFactor * side * DegreesToRadians * friction * momentOfInertia;

	}
	
	
	/// Bounces the ball along the Y-axis
	public void bounceY(double yPos, int side){
		this.pos.y  = yPos;
		this.vel.y *= -bounceCoef*0.7;
		
		/// We compute the relative velocity of the edge of the ball and the wall and
		/// based on the amount of friction this will result in a force that pushes
		/// the ball in a horizontal direction, and reduces or increases the spin. 
		double relativeVelocity = vel.y + (side * spin);  
		double spinFactor = side * ( relativeVelocity * friction ); 
		this.spin -= spinFactor;
		
		this.vel.x += size * spinFactor * side * DegreesToRadians * friction * momentOfInertia;
	}
	
	// These functions return a copy vector in order to avoid another routine tampering 
	// with these values in unexpected ways.

	
	public Vector2d getPos(){
		return new Vector2d(pos);
	}

	public Vector2d getVel(){
		return new Vector2d(vel);
	}
	
	public double getAngle(){
		return angle;
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
	
	public void setSize(double s){
		size = s;
	}
	
	public void resetUnTouchedTime(){
		this.unTouchedTime = 0;
	}
	
	public void setWeight(double w){
		this.weight = w;
	}


	public void setInertia(double i){
		momentOfInertia = i;
	}

	
	public void setGravity(double g){
		gravity = g;
	}

	public void setFriction(double f){
		friction = f;
	}
	
	public void setClickSpin(double s){
		clickSpin = s;
	}

	
	public void setMinTouchTime(int t){
		minTouchTime = t;
	}
	
	public void setMaxSpeed(double m){
		maxSpeed = m;
	}
	
	/// Collides this ball with another ball and performs the required velocity changes on both balls.
	public void collide(Ball otherBall){
		if(otherBall == null) // If the other ball doens't exist, nothing happens.
			return;
		
		/// This method works in a few steps, maybe it should be refactored further into smaller methods.
		/// First we  finds the distance between this ball and the other ball, and the relative velocity.
		
		
		// We then given their positions and velocities we
		// find the times ts = t1, t2 relative to now when the two balls were exactly intersecting
		// We then move both balls back to the earlier step t1 this step so that the balls dont get
		// stuck in one another. After that we solve newtons equations, not including spin and find 
		// the velocities resulting from the collision.

		Vector2d posDiff = this.pos.diff(otherBall.getPos());
		Vector2d velDiff = this.vel.diff(otherBall.getVel());
		
		// The distance at which these two balls should collide. 
		double collideDist = (this.size + otherBall.size) /2;

		
		// Finds the two times when the balls will be intersecting
		double[] ts = findCollisionTime(posDiff, velDiff, collideDist);
		double t1 = ts[0];
//		double t2 = ts[1];
 /// TRY CHANGING THIS AND SEE IF IT DOESNT CRASH IT SHOULD BUT LETS TRY
		if((posDiff.abs() - (size*size))  > 0)
			Log.w("Debuggin", "!!!!!!!!!!!!!!!!!SOmething is messed up :/ !!!!!!!!!!!!!!!!!!!!!!!");

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
		
		
		// We check if the current relative velocities will bring the balls further apart or not.
		// This lets us avoid infinite collision loops as balls moving away from one another will 
		// keep moving. 
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
		
		
		double massFactor = 2 * otherBall.weight/(this.weight + otherBall.weight) ;  
		
		double dist = posDiff.length() * posDiff.length();
		
		Vector2d newForce = posDiff.multret(massFactor * innerProd / dist); 
		
		// We want to add the spin component to their interaction.
		/// This is just a 90 degrees, aka the normal to the direct vector. 
//		Vector2d normal = new Vector2d(-posDiff.y, posDiff.x);
//		double relativeSpin = (this.spin - otherBall.spin)  + (velDiff.cross(posDiff);
//		normal.normalizeToLength(relativeSpin);
//		newForce.add(normal.multret(ballbBallFriction * momentOfInertia * )) 
		
		vel.minus(newForce);
		otherBall.updateForce(newForce.multret(this.weight));
	}
	
	
	/// Checks if the angle between vec1 and vec2 is less than 180 degrees.
	private boolean areSameDirection(Vector2d vec1, Vector2d vec2){
		return (vec1.dot(vec2) >= 0);
	}
	
	/// Finds the time when two balls will collide, given their different positions, velocities 
	/// and the distance at which they collide. 
	private double[] findCollisionTime(Vector2d posDiff, Vector2d velDiff, double collideDistance) {
		double term1 = 2 * posDiff.multPoint(velDiff).sum();

		//  4 (k^2+l^2) (-D^2+x^2+y^2))
		double term2 = 4 * velDiff.abs() * (posDiff.abs() - (collideDistance*collideDistance));
		
		// +2 k x+2 l y)
		double term3 = 2 * (posDiff.multPoint(velDiff)).sum();
		
		// (2 (k^2+l^2))
		double frac = 2 * velDiff.abs();
		
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
	
	/// Returns if this ball can be touched at this time. 
	public boolean canBeTouched(){
		return unTouchedTime > minTouchTime;
	}
	
	public boolean click(Vector2d clickPos, double forceConstant){
		if (unTouchedTime < minTouchTime)
			 return false;
		resetUnTouchedTime();
		
		Vector2d force = pos.diff(clickPos);
		force.normalize();
		force.y = -1;
		force.normalize();
		
		vel = force.multret(forceConstant / weight);
		
		/// Spin interaction constant is the conversation from radians to degrees as well
		this.vel.x += size * spin * DegreesToRadians * friction * momentOfInertia;
		this.spin -= spin * friction;
		
		
		/// This is the normal vector of the surface where we kick
		Vector2d normal = pos.diff(clickPos);
		normal.normalize();
		Vector2d spinVec = new Vector2d(-normal.y, normal.x);
		double spinIncremenent = new Vector2d(0,-1).dot(spinVec) * -clickSpin; 
		this.spin += spinIncremenent;
		
		return true;
	}
	
	
	/// Collides the ball with a swipe from user represented in a list of TouchEvents and an index
	/// i where the swipe first collided with the ball. deltaTime is the total duration of the swipe.
	/// Returns if the ball was actually moved or if it was too recent.
	public boolean drag(Finger finger, int i, double deltaTime){
		/// Returns true if the ball has been clicked too recently. 
		if( unTouchedTime < minTouchTime ){
			Log.w("Debuggin", "We choose to not detect this touch.  ." + vel);
			return false;
		}
		Vector2d touchDir = finger.pos.diff(this.pos);
		touchDir.normalize();
//		updateForce(touchDir.multret(-dragVel.length()/15));
		unTouchedTime = 0;
		return true;
	}	

}
