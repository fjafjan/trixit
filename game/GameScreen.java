package com.trixit.game;
/// Okay so what is left to do to make this actually work? Well first we need
/// To actually move the assets that I have to the assets folder, I think I downloaded a shitty
/// Football but I could always do that again. I should create some kind of menu? It doesnt matter
/// What the hell it looks like I think.




import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import android.graphics.Paint;
import android.graphics.Color;
import android.util.Log;

import com.trixit.framework.Screen;
import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Input.TouchEvent;
import com.trixit.framework.Vector2d;
import com.trixit.game.Ball;
import com.trixit.game.TennisBall;
//import java.util.


public class GameScreen extends Screen {
	// unclear what the purpose of this is...
	enum GameState{
		Ready, Running, Paused, GameOver
	}
	Random random = new Random();
	GameState state = GameState.Ready;

	// Create game objects here....
	Paint paint, paint2;
	
	int score, livesleft;
	
	int ballSize;
	List<Ball> balls;
	TennisBall tennisball;
	double minXPos, maxXPos, minYPos, maxYPos, chanceOfMod, tennisSpeed;
	int gameHeight, gameWidth;
	
	
	//debugging variable/s
	boolean collided = false;
	public GameScreen(Game game){
		super(game);
		tennisSpeed = 20;
		ballSize = 100;
		livesleft = 10;
		chanceOfMod = 0.1;
		score = 0;
		gameHeight = game.getGraphics().getHeight();
		gameWidth =game.getGraphics().getWidth();
		balls = new ArrayList<Ball>();
		balls.add(new Ball(gameWidth/2, gameHeight/2, 0,0));
		// Initialize game object here
		// We start with a single ball at.. some random position on the screen?
		// I should implement a position/couple class.
//		ballX = gameWidth/2;
//		ballY = gameHeight/2;
		
		
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
		updateBall(deltaTime);
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

	private void updateBall(double deltaTime){
		for(int i=0 ; i < balls.size() ; i++){
			balls.get(i).update(deltaTime);
			Vector2d pos = balls.get(i).getPos();
//			double xPos = balls.get(i).getX();
//			double yPos = balls.get(i).getY();
			// We check for collisions
			for(int j=i+1 ; j < balls.size() ; j++){
				Vector2d pos2 = balls.get(j).getPos();

//				double xPos2 = balls.get(j).getX();
//				double yPos2 = balls.get(j).getY();
				double dist = (pos2.diff(pos)).length();
//				Vector2d oldDiff = new Vector2d(pos2.x - pos.x, pos2.y - pos.y);
//				Vector2d newDiff =  (pos2.diff(pos));
//				double olddist = Math.sqrt(((pos2.x - pos.x)*(pos2.x - pos.x)) + ((pos2.y - pos.y)*(pos2.y - pos2.y)));
//				Log.w("Debuggin", "new dist is  " + dist + " and old dist is " + olddist);
//				Log.w("Debuggin", "new diff is  " + newDiff + " and old dist is " + oldDiff);
//				double dist = Math.sqrt((xPos2 - xPos)*(xPos2 - xPos) + (yPos2 - yPos)*(yPos2 - yPos)); 
				if( dist < ballSize){
					balls.get(i).collide(balls.get(j));
				}
			}
			/// We handle edge cases where the ball collides with a edge or wall below.
			
			/// If the ball edge goes outside the left wall
			if(pos.x < ballSize/2){
				double overstep = (ballSize/2 - pos.x);
				balls.get(i).bounceX(ballSize/2 + overstep );
			/// If the ball edge goes outside the right wall
			}else if(pos.x > gameWidth - (ballSize/2)){
				// overstep represent the amount the ball has went outside the 
				// game area.
				double overstep = (pos.x - gameWidth + (ballSize/2));
				balls.get(i).bounceX(gameWidth - (ballSize/2) - overstep);
			}
			
			/// If the edge of the ball goes outside the top wall/roof
			if(pos.y < ballSize/2){
				double overstep = (ballSize/2 - pos.y);
				balls.get(i).bounceY(ballSize/2 + overstep);
			/// If the edge of the ball goes outside the floor
			}else if(pos.y > gameHeight - (ballSize/2)){
				double overstep = (pos.y - gameHeight + (ballSize/2));
				balls.get(i).bounceY(gameHeight- (ballSize/2) - overstep);
				livesleft -= 1;
				if (livesleft == 0){
					Log.w("Debuggin", "Game is over :(");
					state = GameState.GameOver;
				}
			}		
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
//			balls.add(new Ball(gameWidth/2, gameHeight/2,0,0));
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
		
		Log.w("Debuggin", "We try to push the ball in some direction ");
		Vector2d eventPos = new Vector2d(event.x, event.y);
		Vector2d ballPos = balls.get(ballTouched).getPos();
		Vector2d force = ballPos.diff(eventPos);

		force.normalize();
		balls.get(ballTouched).updateForce(force); 
		Log.w("Debuggin", "We touch the ball, the resulting force is " + force.x + " " + force.y);
	}
	
	private void addTennisBall(){
		for(int attempts = 0; attempts < 20 ; attempts++){
			double testX = random.nextDouble() * gameWidth;
			double testY = ((random.nextDouble()*0.5) - 0.5 ) * gameHeight;
			double initVX = random.nextDouble() * tennisSpeed;
			double initVY = Math.abs( random.nextDouble() * tennisSpeed);
			for (int i = 0; i < balls.size(); i++) {
				if (inBall((int)testX, (int) testY, 10) == -1){
					// Add a reasonably high initial velocity
					tennisball = new TennisBall(testX, testY, initVX, initVY);
					return;
				}
			}
		}
	}	
//			balls.add(new Ball(gameWidth/2, gameHeight/2,0,0));

	
	private int inBall(double x, double y, double radius){
		// I should replace ball with "spheroid game object" for the sake of everyone involved.
		// Check for all balls if this coordinate is .. within that.
		Vector2d pos = new Vector2d(x, y);
		for (int i = 0; i < balls.size(); i++) {
			Vector2d ballPos = balls.get(i).getPos(); 
			if ( pos.diff(ballPos).length()  < (ballSize  + radius)){
				return i;
			}
		}
		return -1;
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
