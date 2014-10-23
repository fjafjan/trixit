package com.trixit.game;

import com.trixit.game.Ball;

public class TennisBall extends Ball{
	public boolean destroy;
	
	/// Creates a TennisBall object at posX, posY with velocity velX, velY and a spin.
	public TennisBall(double posX, double posY, double velX, double velY, double spin){
		super(posX,posY,velX,velY, spin);		
		this.size = 20;
		this.weight = 1./30;
		this.bounceCoef = 0.95;
		destroy = false;
	}
	/// If spin is not specified we say it is 0.
	public TennisBall(double posX, double posY, double velX, double velY){
		this(posX, posY, velX, velY, 0);
	}
}
