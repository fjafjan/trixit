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


/// This class handles all the physics engine type stuff, balls moving, colliding and
/// the impact of touching and swiping on the screen. It also keeps track of the game score
/// and when to make a sound. 
public class Engine {
	Random random = new Random();
	GameState state = GameState.Ready;

	List<Ball> balls;
	TennisBall tennisball;
	
	Options options;
	
	/// Variables that change during the course of a game.
	int score, livesleft, highScore;
	/// Variables that are set once and should not change during a game
	int gameHeight, gameWidth, maxBalls, addBallScore, noFailScore, minTouchTime;
	float volume;
	

	public Engine(int gameWidth, int gameHeight){
		// Here we have various options that can be tweaked and adjusted. 
		livesleft = 3;       	/// The number of bounces on the ground allowed. 
		
		// Initializes an Options object, to tweak em go there. 
		this.options = new Options();
		
		// Initialize game object here
		this.gameWidth = gameWidth;					/// The diagonal of the square... no it's just the width.
		this.gameHeight = gameHeight;				/// Yeah yeah. The height of the game.
		score = 0;									/// The score. Duh. 
		noFailScore = 0;							/// The number of points without dropping a ball
		balls = new ArrayList<Ball>();				/// The list of all balls in use.
		addBall();									/// We add the first ball to the game.
													/// but never drawn or updated etc.
		
		
		balls.get(0).configureOptions(options);     /// We set gravity, friction, etc of the balls.

		volume = AudioManager.STREAM_MUSIC;			/// We set the volume of the game to be the 
													/// current music volume
	}
	


	
	/// Updates the position of all the balls, and checks if any of them collide.
	public void updateBalls(double deltaTime){
		/// Increments time. 
		double deltaT = deltaTime * options.slowDown;
		
		
		/// Checks the score and determines if another ball should be added and if so adds it. 
		tryAddBall();

		
		/// Update all regular balls
		for(int i=0 ; i < balls.size() ; i++){
			balls.get(i).update(deltaT);
			Vector2d pos = balls.get(i).getPos();
			// We check for collisions
			Ball collidedWith = inBall(pos, balls.get(i));
			balls.get(i).collide(collidedWith);

			/// Check if a ball hits a wall.
			checkEdges(balls.get(i));			
		}

		/// If we have a tennisball, update that too.
		if(tennisball != null){
			tennisball.update(deltaT);
			checkEdges(tennisball);
			if(tennisball.destroy)
				tennisball = null;
		}
	}
	
	/// Attempts to add a ball to the system, does nothing if score is not high enough.
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
		double initialSpin = 2 * (random.nextDouble() - 0.5) * options.startSpin;
		
		/// If there are no balls, or if the space we want to spawn a ball is empty.
		/// Note that we could actually use half the ballSize here, but making sure that 
		/// the new ball doens't immediately collide with an existing ball seems like a nice 
		/// idea. 
		if (balls.isEmpty() || inBall(gameWidth/2, gameHeight/2, ballSize) == -1){
			balls.add(new Ball(gameWidth/2, gameHeight/2, 0, -options.startSpeed, initialSpin));
			return;
		}
		
		/// There is a ball "blocking" the default spawn.
		for(int attempts = 0; attempts < 100 ; attempts++){
			double testX = random.nextDouble() * gameWidth;
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;	
			if (inBall(testX, testY, ballSize) == -1){
				balls.add(new Ball(testX, testY, 0, -options.startSpeed, initialSpin));
				return;
			}
		}
	}
	
	/// Creates a new "tennisball" at a random position in the upper half
	/// of the playing field, with a truly random direction but a fixed 
	/// initial speed, tennisSpeed. 
	private void addTennisBall(){
		double ballSize = new TennisBall(0,0,0,0).getSize();
		double initialSpin = 2 * (random.nextDouble() - 0.5) * options.startSpin;
		
		for(int attempts = 0; attempts < 100 ; attempts++){
			
			// We make sure that the ball is spawned in the upper half of the playing field.
			double testX = random.nextDouble() * gameWidth; 
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;
			
			if (inBall((int)testX, (int) testY, ballSize) == -1){
				// To actually get a random angle distribution we should sample the angle
				double velAngle = random.nextDouble()*2*3.1415926535;
				double velX = Math.sin(velAngle) * options.tennisSpeed;
				double velY = Math.cos(velAngle) * options.tennisSpeed;
				
				tennisball = new TennisBall(testX,testY,velX,velY, initialSpin);
				
				return;
			}
		}
	}	

	
	
// Checks if a ball with index ballIndex is outside the game area and if so
// call the correct bounce method. 
	private void checkEdges(Ball ball){
		Vector2d pos = ball.getPos();
		double ballSize = ball.getSize();
		
		double minPosX = ballSize/2;  
		double minPosY = ballSize/2;
		double maxPosX = gameWidth - (ballSize/2);
		double maxPosY = gameHeight - (ballSize/2);
		
		// over step represent the amount the ball has went outside the game area.
		double overstep = 0;
		
		// Left side
		if(pos.x < minPosX){ 
			overstep = (minPosX - pos.x);
			ball.bounceX(ballSize/2 + overstep, -1 );
		/// Right side
		}else if(pos.x > maxPosX){	 
			overstep = (pos.x - maxPosX);
			ball.bounceX(maxPosX - overstep, 1);
		}
		
		/// If the edge of the ball goes outside the top wall/roof
		if(pos.y < minPosY){
			overstep = minPosY - pos.y;
			ball.bounceY(minPosY + overstep, -1);
		}else if(pos.y > maxPosY){
			if(ball instanceof TennisBall){
				tennisball.destroy = true;
			}else if(ball instanceof Ball){
				balls.remove(ball);
//				if (balls.size() < score * addBallScore)
				addBall();
				noFailScore = 0;
				livesleft -= 1;
				if (livesleft <= 0){
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
		int ballTouched = inBall(eventPos.x, eventPos.y, options.touchRadius); 
		// If we did not intersect with any ball then we just go back.
		if (ballTouched == -1){
			return;
		}
		
		// We touched the tennis ball
		if (ballTouched == -2){
			// Play a special tennisball sound I think. 
			tennisball.destroy = true;
			return;
		}
		
		
		if(balls.get(ballTouched).canBeTouched()){
			balls.get(ballTouched).click(eventPos, options.forceConstant);
			// Plays one of the kick sounds. 
			playSound(Assets.kicks);
			increaseScore();
		}
	}
	
	public void tryDrag(Finger finger, double deltaTime){
		double deltaT = deltaTime * options.slowDown;
		/// What do we want drag to do? I really want to try the infinite weight idea.
		int ballTouched = inBall(finger.pos, options.touchRadius);
		
		// If we did not intersect with any ball then we just go back.
		if (ballTouched == -1){
			return;
		}
		
		// We touched the tennisball
		if (ballTouched == -2){
			tennisball.destroy = true;
			return;
		}
		
		if(balls.get(ballTouched).canBeTouched()){
			increaseScore();
			
			/// Using the algorithm from http://stackoverflow.com/questions/1073336/circle-line-collision-detection
			Vector2d d = finger.vel.multret(deltaT);
			/// We assume that vel represents d, the direction of the vector.
			
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
			
			// If the finger is moving too slowly, we give it a predetermined push.
			// If the finger is moving too fast, we also give it a larger predetermined push
			// otherwise we base the push on the speed of the finger. 
			// I believe the math here is still broken. it is def to be fixed in a future update
			if(finger.vel.length() * options.dragConstant < options.minFingerSpeed){
				balls.get(ballTouched).click(intersection, options.forceConstant) ;
			}else if(finger.vel.length() * options.dragConstant < options.maxFingerSpeed){
				balls.get(ballTouched).click(intersection, finger.vel.multret(options.dragConstant).length()) ;
			}else{
				balls.get(ballTouched).click(intersection, options.maxFingerSpeed );
			}
		}
	}
	
	private void increaseScore(){
		score += 1;
		noFailScore += 1;
		if (tennisball == null && random.nextDouble() < options.chanceOfMod){
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
