package com.trixit.game;

import com.trixit.game.Ball;

public class TennisBall extends Ball{
	public boolean destroy;
	
	public TennisBall(double posX, double posY, double velX, double velY){
		super(posX,posY,velX,velY);
		this.size = 20;
		this.bounceCoef = 0.95;
		destroy = false;
	}
	@Override
	public void bounceY(double yPos) {
		if(yPos > 0){
			
		}
	}
}
