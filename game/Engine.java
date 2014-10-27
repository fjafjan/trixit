package com.trixit.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.media.AudioManager;
import android.util.Log;

import com.trixit.framework.Sound;
import com.trixit.framework.Vector2d;
import com.trixit.framework.Input.TouchEvent;
import com.trixit.game.GameScreen.GameState;


/// This class handles all the game logic of balls, collisions and touches. 
public class Engine {
	Random random = new Random();
	GameState state = GameState.Ready;

	List<Ball> balls;
	TennisBall tennisball;
	
	/// Variables that change during the course of a game.
	int score, livesleft, highScore;
	/// Variables that are set once and should not change during a game
	int gameHeight, gameWidth, maxBalls, addBallScore, noFailScore;
	double chanceOfMod, tennisSpeed, forceConstant, slowDown, gravity;
	double startSpeed, startSpin, touchRadius;
	
	float volume;

	public Engine(int gameWidth, int gameHeight){
		// Here we have various options that can be tweaked and adjusted. 
		livesleft = 3;       	/// The number of bounces on the ground allowed. 
		chanceOfMod = 0.0;		/// Chance of spawning a tennis ball that in the future will modify the game in some way. 
		forceConstant = 2.4;	/// Linearly increases the force applied by a click. 
		slowDown = 0.60;   		/// Linearly slows down the game. 
		gravity = 0.28;        	/// The gravitational acceleration at every
		startSpeed = 12;		/// The initial vertical speed of a new ball.
		startSpin = 5;			/// The maximum initial spin of a new ball.
		tennisSpeed = 15;     	/// The initial speed of a tennis ball. 
		maxBalls = 3;         	/// The maximum number of balls. 
		addBallScore = 5;    	/// At each increment of this score another ball is added.
		touchRadius = 50;       /// The radius of a touch point for collision detection, aka finger thickness.
		 
		
		// Initialize game object here
		this.gameWidth = gameWidth;					/// The diagonal of the square... no it's just the width.
		this.gameHeight = gameHeight;				/// Yeah yeah. The height of the game.
		score = 0;									/// The score. Duh. 
		noFailScore = 0;							/// The number of points without dropping a ball
		balls = new ArrayList<Ball>();				/// The list of all balls in use.
		addBall();									/// We add the first ball to the game.
													/// but never drawn or updated etc.
		balls.get(0).setSlowDown(slowDown);			/// We set the slowdown factor of ALL balls (static)
		balls.get(0).setGravity(gravity);			/// We set the gravity constant of ALL balls
		balls.get(0).setMinTouchTime(25);			/// The minimum amount of time between touches.

		volume = AudioManager.STREAM_MUSIC;			/// We set the volume of the game to be the 
													/// current music volume
		
		/// TEMPORARY

	}
	

	public void collideDragEvents(ArrayList<DragEvent> dragEvents, double deltaTime) {
		// If we have multiple dragEvents we check each
		for (int i = 0; i < dragEvents.size(); i++) {
			// For each such dragEvent we look at each "click"
			DragEvent thisEvent = dragEvents.get(i);
			ArrayList<TouchEvent> events = thisEvent.getEvents();
			int end = events.size()-1;
			// If this drag event has not moved we treat it as a click. 
			if(events.get(0).x == events.get(end).x && events.get(0).y == events.get(end).y){
				tryTouch(events.get(0));
				continue;
			}
							
			for (int j = 0; j < events.size(); j++) {
				int ballTouched = inBall(events.get(j).x, events.get(j).y, touchRadius);
				if (ballTouched == -2){
					tennisball.destroy = true;
				}else if (ballTouched != -1){
					// Make sure that this ball has not collided with this swipe before.
					Log.w("Debuggin", "Get swope.");
					// Tries to swipe the ball, will return false if too recent.
					if (balls.get(ballTouched).drag(events, j, deltaTime)){
						score += 1;
						noFailScore += 1;
					}
				}
			}
			
			
		}
	}

	public void updateBalls(double deltaTime){

		/// If we have gotten a decent amount of points without messing up we crank it up by adding a ball.
		if( noFailScore > (addBallScore*balls.size()) ){
			if(balls.size() < maxBalls)
				addBall();
		}

		
		/// Update all regular balls
		for(int i=0 ; i < balls.size() ; i++){
			balls.get(i).update(deltaTime);
			Vector2d pos = balls.get(i).getPos();
			// We check for collisions
			
			// Log.w("Debuggin", "Ball " + i + " is at " + balls.get(i).getPos());
			
			Ball collidedWith = inBall(pos, balls.get(i));
			balls.get(i).collide(collidedWith);
			checkEdges(balls.get(i));			
//			int collidedWith = inBall(pos, balls.get(i).getSize()/2.);
//			if( collidedWith != -1 && collidedWith != i){
//				if(collidedWith == -2){
//					Log.w("Debuggin", "We are colliding tennisball");
//					balls.get(i).collide(tennisball);
//				}else if(collidedWith < i){
//					balls.get(i).collide(balls.get(collidedWith));
//				}
//			}
			/// We handle edge cases where the ball collides with a wall below.
		}

		/// If we have a tennisball, update that too.
		if(tennisball != null){
			tennisball.update(deltaTime);
			checkEdges(tennisball);
			if(tennisball.destroy)
				tennisball = null;
		}
	}
	
	/// Creates a new ball, it attempts first to create it at the center of the playing field
	/// but if that is occupied, it will use another random position in the upper half of 
	/// the playing field, with no horizontal speed and a fixed upward vertical velocity. 
	private void addBall(){
		noFailScore = 0;
		double ballSize = new Ball(0,0,0,0).getSize();
		double initialSpin = 2 * (random.nextDouble() - 0.5) * startSpin;
		/// If there are no balls, or if the space we want to spawn a ball is empty.
		/// Note that we could actually use half the ballSize here, but making sure that 
		/// the new ball doens't immediately collide with an existing ball seems like a nice 
		/// idea. 
		if (balls.isEmpty() || inBall(gameWidth/2, gameHeight/2, ballSize) == -1){
			balls.add(new Ball(gameWidth/2, gameHeight/2, 0, -startSpeed, initialSpin));
			return;
		}
		/// There is a ball "blocking" the default spawn.
		for(int attempts = 0; attempts < 100 ; attempts++){
			double testX = random.nextDouble() * gameWidth;
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;	
			if (inBall(testX, testY, ballSize) == -1){
				balls.add(new Ball(testX, testY, 0, -startSpeed, initialSpin));
				return;
			}
		}
	}
	
	/// Creates a new "tennisball" at a random position in the upper half
	/// of the playing field, with a truly random direction but a fixed 
	/// initial speed, tennisSpeed. 
	private void addTennisBall(){
		double ballSize = new TennisBall(0,0,0,0).getSize();
		double initialSpin = 2 * (random.nextDouble() - 0.5) * startSpin;
		
		for(int attempts = 0; attempts < 100 ; attempts++){
			
			// We make sure that the ball is spawned in the upper half of the playing field.
			double testX = random.nextDouble() * gameWidth; 
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;
			
			if (inBall((int)testX, (int) testY, ballSize) == -1){
				// To actually get a random angle distribution we should sample the angle
				double velAngle = random.nextDouble()*2*3.1415926535;
				double velX = Math.sin(velAngle) * tennisSpeed;
				double velY = Math.cos(velAngle) * tennisSpeed;
				
				tennisball = new TennisBall(testX,testY,velX,velY, initialSpin);
				
				return;
			}
		}
	}	

	
	
// Checks if a ball with index ballIndex is outside the game area and if so
// call the correct bounce method. 
	private void checkEdges(Ball ball){
//		Ball ball = balls.get(ballIndex);
		Vector2d pos = ball.getPos();
		double ballSize = ball.getSize();
		
		double minPosX = ballSize/2;  
		double minPosY = ballSize/2;
		double maxPosX = gameWidth - (ballSize/2);
		double maxPosY = gameHeight - (ballSize/2);
		
		// over step represent the amount the ball has went outside the
		double overstep = 0;
		// game area.
		if(pos.x < minPosX){ // Left side
			overstep = (minPosX - pos.x);
			ball.bounceX(ballSize/2 + overstep, -1 );
		/// If the ball edge goes outside the right wall
		}else if(pos.x > maxPosX){ // Right side	 
			overstep = (pos.x - maxPosX);
			ball.bounceX(maxPosX - overstep, 1);
		}
		
		/// If the edge of the ball goes outside the top wall/roof
		if(pos.y < minPosY){
			overstep = minPosY - pos.y;
			ball.bounceY(minPosY + overstep, -1);
		/// If the edge of the ball goes outside the floor
		}else if(pos.y > maxPosY){
			if(ball instanceof TennisBall){
				// Destroy the tennisball
				tennisball.destroy = true;
				Log.w("Debuggin", "We are destroying the tennisball at position" + tennisball.getPos());
			}else if(ball instanceof Ball){
				overstep = pos.y - maxPosY;
				ball.bounceY(maxPosY - overstep, 1);
				balls.remove(ball);
				if (balls.isEmpty())
					addBall();
				noFailScore = 0;
				livesleft -= 1;
				if (livesleft <= 0){
					Log.w("Debuggin", "Game is over :(");
					state = GameState.GameOver;
				}
			}else{
				Log.w("Debuggin", "Somehow the ball is neither orignal or a tennisball. Weird stuff");
			}
		}
		// If we have collided with one of the edges
		if (overstep > 0 && pos.y < maxPosY) {
			playSound(Assets.bounces);
			ball.getY();
		}
		
		ball = null;

	}

	
	
	public void tryTouch(TouchEvent event){
		int ballTouched = inBall(event.x, event.y, touchRadius); 
		// If we did not intersect with any ball then we just go back.
		if (ballTouched == -1){
			return;
		}
		
		// We touched the tennisball
		if (ballTouched == -2){
			// Play a special tennisball sound I think. 
			//double x = startCustomMode(); // think of  abetter name for this and actually do something with it. 
			tennisball.destroy = true;
			return;
		}
		
//		Log.w("Debuggin", "We try to push the ball in some direction ");
		Vector2d eventPos = new Vector2d(event.x, event.y);
		
		if(balls.get(ballTouched).click(eventPos, forceConstant)){
			// Plays one of the kick sounds. 
			playSound(Assets.kicks);

			score += 1;
			noFailScore += 1;
			if (tennisball == null && random.nextDouble() < chanceOfMod){
				addTennisBall();
			}
		}
	}
	

	
	// Determines if the position at pos x, y is within a radius of either any ball or
	// tennis ball. If so, it returns the index of the ball or -2 respectively. If not, 
	// returns -1.
	private int inBall(double x, double y, double radius){
		// I should replace ball with "spheroid g object" for the sake of everyone involved.
		// Check for all balls if this coordinate is .. within that.
		Vector2d pos = new Vector2d(x, y);

		
		// We check if there is a colission with the tennisball
		if(tennisball != null){
			double ballSize = tennisball.getSize();
			Vector2d ballPos = tennisball.getPos();
			Log.w("Debuggin", "We are checking for collision with the tennisball");
			Log.w("Debuggin", "The distance from this ball was " + pos.diff(ballPos).length());
			Log.w("Debuggin", "The tennisball is at pos " + ballPos);
			if ( pos.diff(ballPos).length()  < ((ballSize/2)  + radius)){
				Log.w("Debuggin", "Something was inside a tennisball that I am pretty sure is null");
				Log.w("Debuggin", "Tennisball is at " + tennisball.getPos());
				return -2;
			}
		}

		/// If not, then we check if there a collision with any of the other balls. 
		for (int i = 0; i < balls.size(); i++) {
			double ballSize = balls.get(i).getSize();
			Vector2d ballPos = balls.get(i).getPos();
			if ( pos.diff(ballPos).length()  < ((ballSize/2)  + radius)){
//  			double firstDiff = pos.diff(ballPos).length();
//				double otherDiff = ballPos.diff(pos).length();
//				Log.w("Debuggin", "we find a collision with ball " + i);
				return i;
			}
		}
		return -1;
	}
	
	public int inBall(Vector2d pos, double radius){
		return inBall(pos.x, pos.y, radius);
	}

	/// Similar to above, but checks if the ball b intersects with any other ball and if so returns
	/// that ball, or -1.
	private Ball inBall(Vector2d pos, Ball b){
		double radius = b.getSize()/2;
		int ballIndex = inBall(pos.x, pos.y, radius);
		if(ballIndex == -2){
			return tennisball;
		}else if(ballIndex == -1 || b.equals(balls.get(ballIndex))){
			return null;
		}else{
			return balls.get(ballIndex);
		}
	}

	/// Plays a randomly selected Sound from sounds.	
	private void playSound(Sound[] sounds){
		int length = sounds.length;
		int pick = random.nextInt(length);
		sounds[pick].play(volume);
	}

}
