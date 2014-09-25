package com.trixit.game;
/// Okay so what is left to do to make this actually work? Well first we need
/// To actually move the assets that I have to the assets folder, I think I downloaded a shitty
/// Football but I could always do that again. I should create some kind of menu? It doesnt matter
/// What the hell it looks like I think.




import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Color;
import android.util.Log;

import com.trixit.framework.Screen;
import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Input.TouchEvent;
import com.trixit.game.Ball;
//import java.util.


public class GameScreen extends Screen {
	// unclear what the purpose of this is...
	enum GameState{
		Ready, Running, Paused, GameOver
	}
	
	GameState state = GameState.Ready;

	// Create game objects here....
	Paint paint;
	
	int livesleft = 1;
	int nrOfBalls = 1;
	
	int ballSize;
	List<Ball> balls;
	double minXPos, maxXPos, minYPos, maxYPos;
	int gameHeight, gameWidth;
	
	public GameScreen(Game game){
		super(game);
		ballSize = 100;
		livesleft = 5;
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
	} 

	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		// I think there should only be two states, either running or game over. No 
		// menues and shit, smooth user experience!
		if (state == GameState.Ready)
			updateReady(touchEvents);
		if (state == GameState.Running)
			updateRunning(touchEvents);
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

	private void updateRunning(List<TouchEvent> touchEvents) {
		int len =  touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_DOWN){
				int ballTouched = inBall(event.x, event.y); 
				if (ballTouched != -1){
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
			}
		}
		updateBall();
	}

	private void updateBall(){
		for(int i=0 ; i < nrOfBalls ; i++){
			balls.get(i).update();
			double xPos = balls.get(i).getX();
			double yPos = balls.get(i).getY();
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
	
	private int inBall(int x, int y){
		// should change this to a for loop when multiple balls are implemented. 
		// I should replace ball with "spheroid game object" for the sake of everyone involved.
		double posX, posY;
		for (int i = 0; i < balls.size(); i++) {
			posX = balls.get(i).getX();
			posY = balls.get(i).getY();
			if (x > posX - ballSize/2 && x < posX + ballSize/2 
					&& y > posY - ballSize/2 && y < posY + ballSize + ballSize/2)
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
		
		for (int i = 0; i < balls.size(); i++) {
			int ballX = (int) balls.get(i).getX() - (ballSize/2);
			int ballY = (int)  balls.get(i).getY() - (ballSize/2);
			g.drawImage(Assets.ball, ballX, ballY);			
		}
	}

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        g.drawRect(0, 0, 1281, 801, Color.BLACK);
        g.drawString("GAME OVER.", 640, 300, paint);
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
