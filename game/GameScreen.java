package com.trixit.game;
/// Okay so what is left to do to make this actually work? Well first we need
/// To actually move the assets that I have to the assets folder, I think I downloaded a shitty
/// Football but I could always do that again. I should create some kind of menu? It doesnt matter
/// What the hell it looks like I think.




import java.util.List;

import android.graphics.Paint;
import android.graphics.Color;
import android.util.Log;

import com.trixit.framework.Screen;
import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Input.TouchEvent;
//import java.util.


public class GameScreen extends Screen {
	// unclear what the purpose of this is...
	enum GameState{
		Ready, Running, Paused, GameOver
	}
	
	GameState state = GameState.Ready;

	// Create game objects here....
	int livesleft = 1;
	Paint paint;
	int ballSize;
	double ballX, ballY;
	double ballVX, ballVY;
	double minXPos, maxXPos, minYPos, maxYPos;
	double bounceCoef;
	int gameHeight, gameWidth;
	
	public GameScreen(Game game){
		super(game);
		ballSize = 100;
		bounceCoef = 0.7;
		livesleft = 5;
		gameHeight = game.getGraphics().getHeight();
		gameWidth =game.getGraphics().getWidth();
		
		// Initialize game object here
		// We start with a single ball at.. some random position on the screen?
		// I should implement a position/couple class.
		ballX = gameWidth/2;
		ballY = gameHeight/2;
		ballVX = 0;
		ballVY = 0;
		
		
		paint = new Paint();
		paint.setTextSize(60);
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
				if (inBall(event.x, event.y)){
					// This already assumes that the bPos represents the center
					double xDiff = ballX - event.x;
					double yDiff = ballY - event.y;
					// f = x-xk/|x-xk|
					double xForce = 10 * xDiff / Math.sqrt((xDiff*xDiff) + (yDiff*yDiff));
					double yForce = 10 * yDiff / Math.sqrt((xDiff*xDiff) + (yDiff*yDiff));
					// Change the 0.01 constant to be weight, make sure that the directons
					// are actually correct... just try it out. 
					ballVX += xForce;
					ballVY += yForce;
					Log.w("Debuggin", "We touch the ball, the resulting force is " + xForce + " " + yForce);
				}
			}
		}
		updateBall();
		
		
		
		
	}

	private void updateBall(){
		ballVY += 0.1; // Some kind of gravity. 
		
		ballX += ballVX;
		ballY += ballVY;
		
		if(ballX < ballSize/2){
			double overstep = (ballSize/2 - ballX);
			ballX = ballSize/2 + overstep ;
			ballVX *= -bounceCoef;
		}else if(ballX > gameWidth - (ballSize/2)){
			// overstep represent the amount the ball has went outside the 
			// game area.
			double overstep = (ballX - gameWidth + (ballSize/2));
			ballX = gameWidth - (ballSize/2) - overstep;
			ballVX *= -bounceCoef;
		}

		if(ballY < ballSize/2){
			double overstep = (ballSize/2 - ballY);
			ballY = ballSize/2 + overstep;
			ballVY *= -bounceCoef;
		
		}else if(ballY > gameHeight - (ballSize/2)){
			//Log.w("Debuggin", "We are bouncing, position pre bounce is " + ballY);
			double overstep = (ballY - gameHeight + (ballSize/2));
			ballY = gameHeight- (ballSize/2) - overstep;
			//Log.w("Debuggin", "We are bouncing, position pre bounce is " + ballY);
			ballVY *= -bounceCoef;
			livesleft -= 1;
			if (livesleft == 0)
				state = GameState.GameOver;
		}		
	}
	
	private boolean inBall(int x, int y){
		// should change this to a for loop when multiple balls are implemented. 
		// I should replace ball with "spheroid game object" for the sake of everyone involved. 
		return (x > ballX - ballSize/2&& x < ballX + ballSize/2 
				&& y > ballY - ballSize/2&& y < ballY + ballSize + ballSize/2);
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
		
		//g.drawARGB(155, 0, 0, 0);
		g.drawString("Click to begin", 640, 300, paint);
		g.drawImage(Assets.ball, (int) ballX,(int) ballY);

	}

	private void drawRunningUI() {
		Graphics g = game.getGraphics();
		g.clearScreen(0);
		g.drawImage(Assets.ball, (int) ballX - (ballSize/2),(int) ballY - (ballSize/2));		
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
