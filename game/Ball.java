package com.trixit.game;

import com.trixit.framework.Vector2d;
import com.trixit.game.GameScreen.GameState;

import android.util.Log;

//import java.util.*;

public class Ball {
//	private double xPos, yPos, xVel, yVel;
	private Vector2d pos;
	private Vector2d vel;
	
	public double size, bounceCoef, weight, gravity;
	public Ball(double xPos,double yPos, double xVel, double yVel){
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
	}
	
	public void bounceX(double xPos){
		this.pos.x = xPos;
		this.vel.x *= -bounceCoef;
	}
	
	public void bounceY(double yPos){
		this.pos.y  = yPos;
		this.vel.y *= -bounceCoef*0.7;
	}
	
	public Vector2d getPos(){
		return new Vector2d(pos);
	}

	public Vector2d getVel(){
		return new Vector2d(vel);
	}

	public void collide(Ball otherBall){
		// v' = v + atm constant mass factor * v1 - v2 dot x1 - x2 / r^2 * x1 - x2
		// http://en.wikipedia.org/wiki/Elastic_collision#Two-Dimensional_Collision_With_Two_Moving_Objects
		// posDiff = x1 - x2
		Vector2d posDiff = this.pos.diff(otherBall.getPos());
		// velDiff = x1 - x2
		Vector2d velDiff = this.vel.diff(otherBall.getVel());
		
		// innerProd =  < x1 - x2, v1 - v2 > 
		double innerProd = posDiff.innerProd(velDiff);
		// massFactor = 2 * m2 / (m1 + m2)
		double massFactor = 1 ;  // keep this as 1 atm. otherBall.weight*2. / (this.weight + otherBall.weight);
		// dist = || x1 - x2 || ^ 2 
		double dist = posDiff.length() * posDiff.length();
		
		Vector2d newForce = posDiff.multret(massFactor * innerProd / dist); 
		
		vel.minus(newForce);
		otherBall.updateForce(newForce.multret(weight));		
	}
}
