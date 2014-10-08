package com.trixit.game;




import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import android.graphics.Paint;
import android.graphics.Color;
import android.media.AudioManager;
import android.util.Log;

import com.trixit.framework.Screen;
import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Input.TouchEvent;
import com.trixit.framework.Sound;
import com.trixit.framework.Vector2d;
import com.trixit.game.Ball;
import com.trixit.game.TennisBall;


public class GameScreen extends Screen {
	enum GameState{
		Ready, Running, Paused, GameOver
	}
	Random random = new Random();
	GameState state = GameState.Ready;

	// Create game objects here....
	Paint paint, paint2;
	
	int score, livesleft, maxBalls;
	
	int ballSize;
	List<Ball> balls;
	TennisBall tennisball;
	double chanceOfMod, tennisSpeed;
	int gameHeight, gameWidth;
	float volume;
	
	public GameScreen(Game game){
		super(game);
		tennisSpeed = 10;
		ballSize = 100;
		livesleft = 10;
		chanceOfMod = 0.1;
		score = 0;
		maxBalls = 10;
		gameHeight = game.getGraphics().getHeight();
		gameWidth =game.getGraphics().getWidth();
		balls = new ArrayList<Ball>();
		balls.add(new Ball(gameWidth/2, gameHeight/2, 0,0));
		// Initialize game object here
		
		volume = AudioManager.STREAM_MUSIC;
		
		paint = new Paint();
		paint.setTextSize(30);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setAntiAlias(true);
		
		paint2 = new Paint();
		paint2.setTextSize(100);
		paint2.setTextAlign(Paint.Align.CENTER);
		paint2.setAntiAlias(true);
		paint2.setColor(Color.WHITE);
	} 

	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		// I think there should only be two states, either running or game over. No 
		// menues and shit, smooth user experience!
		if( score > (2*balls.size()) ){
			if(balls.size() < 2)
				addBall();
		}
		if (state == GameState.Ready)
			updateReady(touchEvents);
		if (state == GameState.Running)
			updateRunning(touchEvents, deltaTime);
		if (state == GameState.GameOver)
			updateGameOver(touchEvents);
	}

	// Simply lets the user touch the screen to start the game. 
	private void updateReady(List<TouchEvent> touchEvents) {
		if (touchEvents.size() > 0){
			Log.w("Debuggin", "Game is now running");
			state = GameState.Running;
		}
		
	}

	private void updateRunning(List<TouchEvent> touchEvents, double deltaTime) {
		int len =  touchEvents.size();
		
		ArrayList<DragEvent> dragEvents = new ArrayList<DragEvent>();
		// Change this to be a class member instead of function variable.  
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_DOWN){
				tryTouch(event);
			}else if(event.type == TouchEvent.TOUCH_DRAGGED){
				if(dragEvents.isEmpty()){
					dragEvents.add(new DragEvent(event));
				}else{
					boolean assigned = false;
					for (int j = 0; j < dragEvents.size(); j++) {
						if(dragEvents.get(j).id == event.pointer){
							dragEvents.get(j).addEvent(event);
							assigned = true;
						}
					}
					/// If there was no other drag event associated with
					if(!assigned){
						dragEvents.add(new DragEvent(event));
					}
				}
					// Hopefully all the drag events from one finger appear at least in sequence. 
					
//					Log.w("Debuggin", "We have another touch event of type " + event.type);
					//Log.w("Debuggin", "For reference UP = " + TouchEvent.TOUCH_UP  + " dragged = " + TouchEvent.TOUCH_DRAGGED);			
			}
		}		
		// We have checked all our touch events.
		collideDragEvents(dragEvents, deltaTime);
		// Moves all balls forward in time and checks for collisions.
		updateBalls(deltaTime);
	}

	private void collideDragEvents(ArrayList<DragEvent> dragEvents, double deltaTime) {
		// If we have multiple dragEvents we check each
		for (int i = 0; i < dragEvents.size(); i++) {
			// For each such dragEvent we look at each "click"
			DragEvent thisEvent = dragEvents.get(i);
			ArrayList<TouchEvent> events = thisEvent.getEvents();
			int end = events.size()-1;
			if(events.get(0).x == events.get(end).x && events.get(0).y == events.get(end).y){
				tryTouch(events.get(0));
				continue;
			}
							
			for (int j = 0; j < events.size(); j++) {
				int ballTouched = inBall(events.get(j).x, events.get(j).y, 0);
				if (ballTouched != -1){
					// Make sure that this ball has not collided with this swipe before.
					Log.w("Debuggin", "Get swope.");
					// Tries to swipe the ball, will return false if too recent.
					if (balls.get(ballTouched).drag(events, j, deltaTime))
						score += 1;
				}
			}
			
			
		}
	}

	private void printDragEvents(ArrayList<DragEvent> dragEvents) {
		for (int i = 0; i < dragEvents.size(); i++) {
			Log.w("Debuggin", "Dragevent nr " + dragEvents.get(i).id);
			dragEvents.get(i).printEvents();
		}
	}

	private void updateBalls(double deltaTime){
		for(int i=0 ; i < balls.size() ; i++){
			balls.get(i).update(deltaTime);
		}
		for(int i=0 ; i < balls.size() ; i++){
			Vector2d pos = balls.get(i).getPos();
			// We check for collisions
			
			// THE FACT THAT WE DON'T UPDATE ALL BALLS FIRST AND CHECK FOR COLISSIONS AFTERWARDS
			// IS ALMOST CERTAINLY WHY I STILL HAVE SOME WEIRD COLISSION PATTERNS!!
			for(int j=i+1 ; j < balls.size() ; j++){
				Vector2d pos2 = balls.get(j).getPos();

				double dist = (pos2.diff(pos)).length();

				if( dist < ballSize){
					balls.get(i).collide(balls.get(j));
				}
			}
			/// We handle edge cases where the ball collides with a edge or wall below.
			checkEdges(balls.get(i));			
		}
	}
	
	private void addBall(){
		if (inBall((int) gameWidth/2, (int) gameHeight/2, ballSize) == -1){
			balls.add(new Ball((int) gameWidth/2, (int) gameHeight/2, 0, 0));
			return;
		}
		for(int attempts = 0; attempts < 100 ; attempts++){
			double testX = random.nextDouble() * gameWidth;
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;
			for (int i = 0; i < balls.size(); i++) {
				if (inBall((int)testX, (int) testY, ballSize) == -1){
					balls.add(new Ball(testX, testY, 0, 0));
					return;
				}
			}
		}
	}

	// This is virtually identical to the above code, pretty sure I can do better
	// than this...
	private void addTennisBall(){
		tennisball = new TennisBall(0,0,0,0);
		double ballSize = tennisball.getSize();
		for(int attempts = 0; attempts < 100 ; attempts++){
			double testX = random.nextDouble() * gameWidth;
			// We make sure that the ball is spawned in the upper half of the playing field. 
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;			
			for (int i = 0; i < balls.size(); i++) {
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
		
		// overstep represent the amount the ball has went outside the
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
			if(ball instanceof Ball){
				overstep = pos.y - maxPosY;
				ball.bounceY(maxPosY - overstep);
				
				livesleft -= 1;
				if (livesleft <= 0){
					Log.w("Debuggin", "Game is over :(");
					state = GameState.GameOver;
				}
			}else if(ball instanceof TennisBall){
				// Destroy the tennis ball
				tennisball = null;
			}else{
				Log.w("Debuggin", "Somehow the ball is entiher orignal or a tennisball. Weird stuff");
			}
		}		
		// If we have collided with one of the edges
		if (overstep > 0) {
			playSound(Assets.bounces);
		}

	}

	
	
	private void tryTouch(TouchEvent event){
		int ballTouched = inBall(event.x, event.y, 0); 
		// If we did not intersect with any ball then we just go back.
		if (ballTouched == -1){
			return;
		}

		score += 1;
		if (random.nextDouble() < chanceOfMod){
			addTennisBall();
		}
		// Plays one of the kick sounds. 
		playSound(Assets.kicks);
		
		Log.w("Debuggin", "We try to push the ball in some direction ");
		Vector2d eventPos = new Vector2d(event.x, event.y);
		Vector2d ballPos = balls.get(ballTouched).getPos();
		Vector2d force = ballPos.diff(eventPos);

		force.normalize();
		balls.get(ballTouched).updateForce(force); 
		Log.w("Debuggin", "We touch the ball, the resulting force is " + force.x + " " + force.y);
	}
	

	// Determines if the position at pos x, y is within a radius of either any ball or
	// tennis ball. If so, it returns the index of the ball or -2 respectively. If not, 
	// returns -1.
	private int inBall(double x, double y, double radius){
		// I should replace ball with "spheroid game object" for the sake of everyone involved.
		// Check for all balls if this coordinate is .. within that.
		Vector2d pos = new Vector2d(x, y);
		for (int i = 0; i < balls.size(); i++) {
			double ballSize = balls.get(i).getSize();
			Vector2d ballPos = balls.get(i).getPos(); 
			if ( pos.diff(ballPos).length()  < (ballSize  + radius)){
				return i;
			}
		}
		if(tennisball != null){
			double ballSize = tennisball.getSize();
			Vector2d ballPos = tennisball.getPos();
			if ( pos.diff(ballPos).length()  < (ballSize  + radius)){
				return -2;
			}
		}
		return -1;
	}
	
	private void playSound(Sound[] sounds){
		int length = sounds.length;
		int pick = random.nextInt(length);
		sounds[pick].play(volume);
	}
	
	private void updateGameOver(List<TouchEvent> touchEvents) {
		for (int i = 0; i < touchEvents.size(); i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_DOWN) {
				nullify();
				game.setScreen(new MainScreen(game));
				return;	
			}
		}
	}


	@Override
	public void paint(float deltaTime) {
		switch (state) {
		case Ready: 
			drawReadyUI();
			break;
		case Running:
			drawRunningUI();
			break;
		case GameOver:
			drawGameOverUI();
			break;
		default:
			break;
		}
	}

	private void nullify() {
		paint = null;
		System.gc();
	}

	private void drawReadyUI() {
		Graphics g = game.getGraphics();

		g.drawString("Click to begin", 640, 300, paint);
//		for (int i = 0; i < balls.size(); i++) {
//			g.drawImage(Assets.ball, (int) balls.get(i).getX(),(int) balls.get(i).getY());			
//		}
	}

	private void drawRunningUI() {
		Graphics g = game.getGraphics();
		g.clearScreen(0);
		
		g.drawString("Score : " + score, gameWidth - 300, 150, paint2);
		g.drawString("Lives left : " + livesleft, 100, 150, paint2);
		for (int i = 0; i < balls.size(); i++) {
			int ballX = (int) balls.get(i).getX() - (ballSize/2);
			int ballY = (int)  balls.get(i).getY() - (ballSize/2);
			g.drawImage(Assets.ball, ballX, ballY);			
		}
	}

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        g.drawRect(0, 0, 1281, 801, Color.BLACK);
        g.drawString("GAME OVER.", gameWidth/2, gameHeight/2, paint2);
    }

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void backButton() {
		nullify();
	}

}
