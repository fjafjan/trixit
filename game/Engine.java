package com.trixit.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.media.AudioManager;
import android.util.Log;

import com.trixit.framework.Sound;
import com.trixit.framework.Vector2d;
import com.trixit.framework.Input.TouchEvent;
import com.trixit.game.GameScreen.GameState;


/// This class handles all the game logic of balls, collisions and touches. 
public class Engine {
	double id;
	Random random = new Random();
	GameState state = GameState.Ready;
	SharedPreferences settings;

	// Create game objects here....
	Paint paint, paint2;
	
	int score, livesleft, maxBalls, addBallScore, noFailScore;
	
//	int ballSize;
	List<Ball> balls;
	Ball testBall;
	TennisBall tennisball;
	double chanceOfMod, tennisSpeed, forceConstant, slowDown, gravity;
	double touchRadius, livesIndFactor;
	int gameHeight, gameWidth, startSpeed, highScore;
	float volume;

	public Engine(double d){
		id = d;
		// Here we have various options that can be tweaked and adjusted. 
		livesleft = 3;       	/// The number of bounces on the ground allowed. 
		chanceOfMod = 0.0;		/// Chance of spawning a tennis ball that in the future will modify the game in some way. 
		forceConstant = 1.9;	/// Linearly increases the force applied by a click. 
		slowDown = 0.09;		/// Linearly slows down the game. 
		gravity = 0.28;        	/// The gravitational acceleration at every
		startSpeed = 12;		/// The initial vertical speed of a new ball.
		tennisSpeed = 15;     	/// The initial speed of a tennis ball. 
		maxBalls = 3;         	/// The maximum number of balls. 
		addBallScore = 5;    	/// At each increment of this score another ball is added.
		touchRadius = 35;       /// The radius of a touch point for collision detection, aka finger thickness.
		livesIndFactor = 0.4;	/// How much smaller the little balls indicating lives left are. 
		
		// Initialize game object here
		gameHeight = 1000;							/// Yeah yeah. The height of the game.
		gameWidth = 2000;	/// The diagonal of the square... no it's just the width.
		score = 0;									/// The score. Duh. 
		noFailScore = 0;							/// The number of points without dropping a ball
		balls = new ArrayList<Ball>();				/// The list of all balls in use.
		addBall();									/// We add the first ball to the game.
		testBall = new Ball(0,0,0,0);         		/// Used for getting various ball properties, 
													/// but never drawn or updated etc.
		balls.get(0).setSlowDown(slowDown);			/// We set the slowdown factor of ALL balls (static)
		balls.get(0).setGravity(gravity);			/// We set the gravity constant of ALL balls 

		volume = AudioManager.STREAM_MUSIC;			/// We set the volume of the game to be the 
													/// current music volume
		
		/// EMPORARY
		if( noFailScore > (addBallScore*balls.size()) ){
			if(balls.size() < maxBalls)
				addBall();
		}

	}
	

	private void collideDragEvents(ArrayList<DragEvent> dragEvents, double deltaTime) {
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

//	private void printDragEvents(ArrayList<DragEvent> dragEvents) {
//		for (int i = 0; i < dragEvents.size(); i++) {
//			Log.w("Debuggin", "Dragevent nr " + dragEvents.get(i).id);
//			dragEvents.get(i).printEvents();
//		}
//	}

	private void updateBalls(double deltaTime){
		for(int i=0 ; i < balls.size() ; i++){
			balls.get(i).update(deltaTime);
		}
		if(tennisball != null){
			tennisball.update(deltaTime);
			checkEdges(tennisball);
			if(tennisball.destroy)
				tennisball = null;
		}
		for(int i=0 ; i < balls.size() ; i++){
			Vector2d pos = balls.get(i).getPos();
			// We check for collisions
			
			int collidedWith = inBall(pos, balls.get(i).getSize()/2.);
			if( collidedWith != -1 && collidedWith != i){
				if(collidedWith == -2){
					Log.w("Debuggin", "We are colliding tennisball");
					balls.get(i).collide(tennisball);
				}else if(collidedWith < i){
					balls.get(i).collide(balls.get(collidedWith));
				}
			}
			/// We handle edge cases where the ball collides with a wall below.
			checkEdges(balls.get(i));			
		}
	}
	
	/// Creates a new ball, it attempts first to create it at the center of the playing field
	/// but if that is occupied, it will use another random position in the upper half of 
	/// the playing field, with no horizontal speed and a fixed upward vertical velocity. 
	private void addBall(){
		noFailScore = 0;
		double ballSize = tennisball.getSize();
		
		/// If there are no balls, or if the space we want to spawn a ball is empty.
		/// Note that we could actually use half the ballSize here, but making sure that 
		/// the new ball doens't immediately collide with an existing ball seems like a nice 
		/// idea. 
		if (balls.isEmpty() || inBall(gameWidth/2, gameHeight/2, ballSize) == -1){
			balls.add(new Ball((int) gameWidth/2, (int) gameHeight/2, 0, -startSpeed));
			return;
		}
		/// There is a ball "blocking" the default spawn.
		for(int attempts = 0; attempts < 100 ; attempts++){
			double testX = random.nextDouble() * gameWidth;
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;	
			if (inBall(testX, testY, ballSize) == -1){
				balls.add(new Ball(testX, testY, 0, -startSpeed));
				return;
			}			
		}
	}
	
	/// Creates a new "tennisball" at a random position in the upper half
	/// of the playing field, with a truly random direction but a fixed 
	/// initial speed, tennisSpeed. 
	private void addTennisBall(){
		tennisball = new TennisBall(0,0,0,0);
		double ballSize = tennisball.getSize();
		for(int attempts = 0; attempts < 100 ; attempts++){
			double testX = random.nextDouble() * gameWidth;
			// We make sure that the ball is spawned in the upper half of the playing field. 
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;			
			if (inBall((int)testX, (int) testY, ballSize) == -1){
				tennisball.setPos(new Vector2d(testX, testY));
				// To actually get a random angle distribution we should sample the angle
				double VelAngle = random.nextDouble()*2*3.1415926535;
				double VelX = Math.sin(VelAngle) * tennisSpeed;
				double VelY = Math.cos(VelAngle) * tennisSpeed;
				tennisball.setVel(new Vector2d(VelX, VelY));
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
		if(pos.x < minPosX){
			overstep = (minPosX - pos.x);
			ball.bounceX(ballSize/2 + overstep );
		/// If the ball edge goes outside the right wall
		}else if(pos.x > maxPosX){			 
			overstep = (pos.x - maxPosX);
			ball.bounceX(maxPosX - overstep);
		}
		
		/// If the edge of the ball goes outside the top wall/roof
		if(pos.y < minPosY){
			overstep = minPosY - pos.y;
			ball.bounceY(minPosY + overstep);
		/// If the edge of the ball goes outside the floor
		}else if(pos.y > maxPosY){
			if(ball instanceof TennisBall){
				// Destroy the tennisball
				tennisball.destroy = true;
				Log.w("Debuggin", "We are destroying the tennisball at position" + tennisball.getPos());
			}else if(ball instanceof Ball){
				overstep = pos.y - maxPosY;
				ball.bounceY(maxPosY - overstep);
				balls.remove(ball);
				if (balls.isEmpty())
					addBall();
				noFailScore = 0;
				livesleft -= 1;
				if (livesleft <= 0){
					Log.w("Debuggin", "Game is over :(");
					checkHighScore();
					state = GameState.GameOver;
				}
			}else{
				Log.w("Debuggin", "Somehow the ball is neither orignal or a tennisball. Weird stuff");
			}
		}
		// If we have collided with one of the edges
		if (overstep > 0) {
			playSound(Assets.bounces);
		}

	}

	
	
	private void tryTouch(TouchEvent event){
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
				Log.w("Debuggin", "we find a collision with ball " + i);
				return i;
			}
		}
		return -1;
	}
	
	public int inBall(Vector2d pos, double radius){
		return inBall(pos.x, pos.y, radius);
	}

	private int getHighScore(){		
		if(settings.contains("highScore")){
			highScore = settings.getInt("highScore", 0);
			return highScore;
		}else{
			Editor edit = settings.edit();
			edit.putInt("highScore", 0);
			edit.commit();
			return 0;
		}
	}

	private void checkHighScore(){
		if (score > highScore){
			Editor edit = settings.edit();
			edit.putInt("highScore", score);
			edit.commit();
		}
	}

	private void playSound(Sound[] sounds){
		int length = sounds.length;
		int pick = random.nextInt(length);
		sounds[pick].play(volume);
	}
	
}
