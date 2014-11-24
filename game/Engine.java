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
	int gameHeight, gameWidth, maxBalls, addBallScore, noFailScore, minTouchTime;
	double chanceOfMod, tennisSpeed, forceConstant, dragConstant, slowDown, gravity, friction, touchRadius;
	double startSpeed, startSpin, momentOfInertia, clickSpin, maxFingerSpeed, minFingerSpeed;
	double relativeWeight;
	float volume;
	

	public Engine(int gameWidth, int gameHeight){
		// Here we have various options that can be tweaked and adjusted. 
		livesleft = 3;       	/// The number of bounces on the ground allowed. 
		chanceOfMod = 0.0;		/// Chance of spawning a tennis ball that in the future will modify the game in some way. 
		forceConstant = 0.18;	/// Linearly increases the force applied by a click.
		dragConstant = 0.25;	/// Linearly increases the force applied by a swipe.
		
		slowDown = 1;   		/// Linearly slows down the game. 
		gravity = 0.004;       	/// The gravitational acceleration at every
		friction = 0.5;			/// The amount of interaction between spin and velocity.
		momentOfInertia = 1.4;	/// The strength of the interaction between spin and velocity.
		clickSpin = 0.5;		/// The relative amount of spin a click produces. 
		
		startSpeed = 0.1;		/// The initial vertical speed of a new ball.
		startSpin = 0.6;		/// The maximum initial spin of a new ball.
		tennisSpeed = 15;     	/// The initial speed of a tennis ball. 
		maxBalls = 3;         	/// The maximum number of balls. 
		addBallScore = 10;    	/// At each increment of this score another ball is added.
		touchRadius = 50;       /// The radius of a touch point for collision detection, aka finger thickness.
		minTouchTime = 250;
		relativeWeight = 0.5;
		
		maxFingerSpeed = 0.4;
		minFingerSpeed = 0.1;
		
		// Initialize game object here
		this.gameWidth = gameWidth;					/// The diagonal of the square... no it's just the width.
		this.gameHeight = gameHeight;				/// Yeah yeah. The height of the game.
		score = 0;									/// The score. Duh. 
		noFailScore = 0;							/// The number of points without dropping a ball
		balls = new ArrayList<Ball>();				/// The list of all balls in use.
		addBall();									/// We add the first ball to the game.
													/// but never drawn or updated etc.
		balls.get(0).setGravity(gravity);			/// We set the gravity constant of ALL balls
		balls.get(0).setMinTouchTime(minTouchTime);	/// The minimum amount of time between touches.
		balls.get(0).setFriction(friction); 		/// The amount of interaction between spin and velocity.
		balls.get(0).setInertia(momentOfInertia);	/// The strength of the interaction between spin and vel.
		balls.get(0).setClickSpin(clickSpin);		/// The amount of spin clicking creates.  

		volume = AudioManager.STREAM_MUSIC;			/// We set the volume of the game to be the 
													/// current music volume
		
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

	
	/// Updates the position of all the balls, and checks if any of them collide.
	public void updateBalls(double deltaTime){

		double deltaT = deltaTime * slowDown;
		/// Checks the score and determines if another ball should be added and if so adds it. 
		tryAddBall();

		
		/// Update all regular balls
		for(int i=0 ; i < balls.size() ; i++){
			balls.get(i).update(deltaT);
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
			tennisball.update(deltaT);
			checkEdges(tennisball);
			if(tennisball.destroy)
				tennisball = null;
		}
	}
	
	/// 
	private void tryAddBall(){
//		if( noFailScore > (addBallScore*balls.size()) ){
		if(score > (addBallScore * ( balls.size() * balls.size()))){
			if(balls.size() < maxBalls)
				addBall();
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
//				if (balls.size() < score * addBallScore)
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

	/// Sees if the touchevent event intersects with any ball, and if so 
	/// notifies the ball and increases the score. 
	public void tryTouch(TouchEvent event){
		Vector2d pos = new Vector2d(event.x, event.y);
		tryTouch(pos);
	}
	
	/// Checks if touching at position eventPos intersects with any valid ball
	/// and if so notifies the ball and updates the score. 
	public void tryTouch(Vector2d eventPos){
		int ballTouched = inBall(eventPos.x, eventPos.y, touchRadius); 
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
				
		if(balls.get(ballTouched).canBeTouched()){
			balls.get(ballTouched).click(eventPos, forceConstant);
			// Plays one of the kick sounds. 
			playSound(Assets.kicks);

			increaseScore();
		}
	}
	
	public void tryDrag(Finger finger, double deltaTime){
		double deltaT = deltaTime * slowDown;
		/// What do we want drag to do? I really want to try the infinite weight idea.
		int ballTouched = inBall(finger.pos, touchRadius);
		
		// If we did not intersect with any ball then we just go back.
		if (ballTouched == -1){
			return;
		}
		
		// We touched the tennisball
		if (ballTouched == -2){
			// Play a special tennisball sound I think. 
			tennisball.destroy = true;
			return;
		}
		
		if(balls.get(ballTouched).canBeTouched()){
			Log.w("Debuggin", "We think that we should have a drag");
			
			increaseScore();
			
			/// Using the algorithm from http://stackoverflow.com/questions/1073336/circle-line-collision-detection
			Vector2d d = finger.vel.multret(deltaT);
			/// We assume that vel represents d, the firection of the vector.
			
			Vector2d startPos = finger.pos.diff(d);
			Vector2d ballCenterPos = balls.get(ballTouched).getPos();
			Vector2d f = startPos.diff(ballCenterPos);

			double r = balls.get(ballTouched).getSize();
			
			double a = d.dot(d);
			double b = 2*f.dot(d);
			double c = f.dot(f) - (r*r);
			double discriminant = (b*b) - (4*a*c);
			Vector2d intersection = new Vector2d(0,0);
			
			if(discriminant < 0){	
				// No intersection
				Log.w("Debuggin", "Discriminant determines we have no collisiion");
				return;
			}else{
				discriminant = Math.sqrt(discriminant);
				double t1 = (-b - discriminant)/(2*a);
				double t2 = (-b + discriminant)/(2*a);
				
				intersection = startPos.add(d.multret(t1));
//				if(t1 >= 0 && t1 <= 1){
//					// The intersection is what we want.
//					
//					intersection = startPos.add(d.multret(t1));
//					Log.w("Debuggin", "The naive intersection is aat" + intersection);
//				}else if(t2 >= 0 && t2 <= 1){
//					intersection = startPos.add(d.multret(t1));
//				}else if(t1 <= 0 && t2 >=0){
//					intersection = startPos.add(d.multret(t1));
//				}else{
//					// Okay so what reasonably happens is that the entire line is inside the ball, since our touch is prob inside the ball and the length of the vector is quite short. 
//					// Discriminant is still too dman big though. 
//					Log.w("Debuggin", "We dont find a good intersection and dont set it at all");
//					tryTouch(startPos);
//					return;
//				}
			}
			
			Log.w("Debuggin", "The distance from intersection to ball center is " + intersection.diff(startPos).length());
			Log.w("Debuggin", "The finger velocity is " + finger.vel.length());
			if(finger.vel.length() * dragConstant < minFingerSpeed){
				balls.get(ballTouched).click(intersection, forceConstant) ;
			}else if(finger.vel.length() * dragConstant < maxFingerSpeed){
				balls.get(ballTouched).click(intersection, finger.vel.multret(dragConstant).length()) ;
			}else{
				balls.get(ballTouched).click(intersection, maxFingerSpeed );
			}
			/// We move the new very close to the edge. 
//			intersection = intersection.diff(finger.vel.normalizeToLength(r));
//			Ball fingerBall = new Ball(intersection, finger.vel.multret(deltaT));
//			balls.add(fingerBall);
//			Log.w("Debuggin", "The velocity of this new ball is " + finger.vel.multret(deltaTime));
//			Log.w("Debuggin", "The velocity of the old ball is " + balls.get(ballTouched).getVel());
//			double ballWeight = fingerBall.weight;
//			fingerBall.setWeight(ballWeight * relativeWeight); // close to max int
//			balls.get(ballTouched).collide(fingerBall);
//			balls.get(ballTouched).setVel(balls.get(ballTouched).getVel().multret(0.02)); // A dampening.
//			fingerBall.collide(balls.get(ballTouched));
		}
		
		// Try to not drag on an existing ball :3
//		balls.add(fingerBall);
		// We could try adding this ball to balls and seeing how stupidly fast this becomes. I should
		// change vel to represent the real velocity and have breaks elsewhere. 
	}
	
	private void increaseScore(){
		score += 1;
		noFailScore += 1;
		if (tennisball == null && random.nextDouble() < chanceOfMod){
			addTennisBall();
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
			if ( pos.diff(ballPos).length()  < ((ballSize/2)  + radius)){
				return -2;
			}
		}

		/// If not, then we check if there a collision with any of the other balls. 
		for (int i = 0; i < balls.size(); i++) {
			double ballSize = balls.get(i).getSize();
			Vector2d ballPos = balls.get(i).getPos();
			if ( pos.diff(ballPos).length()  < ((ballSize/2)  + radius)){
				return i;
			}
		}
		return -1;
	}
	
	/// Returns if there is a ball within radius at position pos.
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
