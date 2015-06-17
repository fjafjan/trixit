package com.trixit.game;

import java.util.ArrayList;

public class Options {
	int gameHeight, gameWidth, maxBalls, addBallScore, noFailScore, minTouchTime;
	double chanceOfMod, tennisSpeed, forceConstant, dragConstant, slowDown, gravity, friction, touchRadius;
	double startSpeed, startSpin, momentOfInertia, clickSpin, maxFingerSpeed, minFingerSpeed;
	double relativeWeight;
	float volume;	
	
	// Initialises this options tuple with values I have determined to be good.
	public Options(){ 
		chanceOfMod = 0.0;		/// Chance of spawning a tennis ball that in the future will modify the game in some way. 
		forceConstant = 0.18;	/// Linearly increases the force applied by a click.
		dragConstant = 0.25;	/// Linearly increases the force applied by a swipe.
		
		slowDown = 1;   		/// Linearly slows down the game. 
		gravity = 0.004;       	/// The gravitational acceleration.
		friction = 0.5;			/// The amount of interaction between spin and velocity.
		momentOfInertia = 0.7;	/// The strength of the interaction between spin and velocity.
		clickSpin = 0.5;		/// The relative amount of spin a click produces. 
		
		startSpeed = 0.2;		/// The initial vertical speed of a new ball.
		startSpin = 0.6;		/// The maximum initial spin of a new ball.
		tennisSpeed = 15;     	/// The initial speed of a tennis ball. 
		maxBalls = 3;         	/// The maximum number of balls. 
		addBallScore = 10;    	/// At each increment of this score another ball is added.
		touchRadius = 50;       /// The radius of a touch point for collision detection, aka finger thickness.
		minTouchTime = 250;
		relativeWeight = 0.5;	
		
		maxFingerSpeed = 0.4;
		minFingerSpeed = 0.1;
	}
}
