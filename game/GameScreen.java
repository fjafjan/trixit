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
				int ballTouched = inBall(event.x, event.y, 0); 
				if (ballTouched != -1){
					score += 1;
					if (random.nextDouble() < chanceOfMod){
						addTennisBall();
					}
					// This already assumes that the bPos represents the center
					double xDiff = balls.get(ballTouched).getX() - event.x;
					double yDiff = balls.get(ballTouched).getY() - event.y;
					double xForce = xDiff / Math.sqrt((xDiff*xDiff) + (yDiff*yDiff));
					double yForce = yDiff / Math.sqrt((xDiff*xDiff) + (yDiff*yDiff));
					// Change the 0.01 constant to be weight, make sure that the directons
					// are actually correct... just try it out. 
					balls.get(ballTouched).updateForce(xForce, yForce); 
					Log.w("Debuggin", "We touch the ball, the resulting force is " + xForce + " " + yForce);
				}
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
		printDragEvents(dragEvents);
		updateBall(deltaTime);
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
			double xPos = balls.get(i).getX();
			double yPos = balls.get(i).getY();
			// We check for collisions
			for(int j=i+1 ; j < balls.size() ; j++){
				double xPos2 = balls.get(j).getX();
				double yPos2 = balls.get(j).getY();
				double dist = Math.sqrt((xPos2 - xPos)*(xPos2 - xPos) + (yPos2 - yPos)*(yPos2 - yPos)); 
				if( dist < ballSize){
					balls.get(i).collide(balls.get(j));
				}
			}
			
			if(xPos < ballSize/2){
				double overstep = (ballSize/2 - xPos);
				xPos = ballSize/2 + overstep ;
				balls.get(i).bounceX(xPos);
			}else if(xPos > gameWidth - (ballSize/2)){
				// overstep represent the amount the ball has went outside the 
				// game area.
				double overstep = (xPos - gameWidth + (ballSize/2));
				xPos = gameWidth - (ballSize/2) - overstep;
				balls.get(i).bounceX(xPos);
			}
	
			if(yPos < ballSize/2){
				double overstep = (ballSize/2 - yPos);
				yPos = ballSize/2 + overstep;
				balls.get(i).bounceY(yPos);			
			}else if(yPos > gameHeight - (ballSize/2)){
				double overstep = (yPos - gameHeight + (ballSize/2));
				yPos = gameHeight- (ballSize/2) - overstep;
				balls.get(i).bounceY(yPos);
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

	
	private int inBall(int x, int y, double radius){
		// I should replace ball with "spheroid game object" for the sake of everyone involved.
		double posX, posY;
		// Check for all balls if this coordinate is .. within that.
		for (int i = 0; i < balls.size(); i++) {
			posX = balls.get(i).getX();
			posY = balls.get(i).getY();
			if ( Math.sqrt( ((x-posX)*(x-posX)) +  ((y-posY)*(y-posY)) ) < ballSize  + radius)
				return i;
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
		for (int i = 0; i < balls.size(); i++) {
			g.drawImage(Assets.ball, (int) balls.get(i).getX(),(int) balls.get(i).getY());			
		}
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
