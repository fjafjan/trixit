package com.trixit.game;




import java.util.List;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.graphics.Color;
import android.media.AudioManager;
import android.util.Log;

import com.trixit.framework.Screen;
import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Input.TouchEvent;
import com.trixit.framework.implementation.AndroidGraphics;
import com.trixit.game.Ball;
import com.trixit.game.TennisBall;


public class GameScreen extends Screen {
	enum GameState{
		Ready, Running, Paused, GameOver
	}
	
	Engine engine;

	// Create game objects here....
	Paint paint, paint2;

	SharedPreferences settings;

	Ball testBall;
	double livesIndFactor;
	int gameHeight, gameWidth, highScore;
	float volume;
	
	
	
	public GameScreen(Game game){
		super(game);
		livesIndFactor = 0.4;	/// How much smaller the little balls indicating lives left are.		
		
		testBall = new Ball(0,0,0,0);         		/// Used for getting various ball properties, 
		// Initialize game object here
		gameHeight = game.getGraphics().getHeight();/// Yeah yeah. The height of the game.
		gameWidth =game.getGraphics().getWidth();	/// The diagonal of the square... no it's just the width.
		engine = new Engine(gameWidth, gameHeight); /// The engine that keeps track of physics and interactions

		
		volume = AudioManager.STREAM_MUSIC;			/// We set the volume of the game to be the 
													/// current music volume
		
		paint = new Paint();						/// We create two separate paints, I think this is bad.
		paint.setTextSize(30);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setAntiAlias(true);
		
		paint2 = new Paint();
		paint2.setTextSize(30);
		paint2.setTextAlign(Paint.Align.CENTER);
		paint2.setAntiAlias(true);
		paint2.setColor(Color.WHITE);

		settings = game.getSettings();				/// A settings object storing variables between games.
		highScore = getHighScore();					/// The high score on this device. Duh. 
		
	} 

	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		// I think there should only be two states, either running or game over. No 
		// menues and shit, smooth user experience!
		if (engine.state == GameState.Ready)
			updateReady(touchEvents);
		if (engine.state == GameState.Running)
			updateRunning(touchEvents, deltaTime);
		if (engine.state == GameState.GameOver)
			updateGameOver(touchEvents);
	}

	// Simply lets the user touch the screen to start the game. 
	private void updateReady(List<TouchEvent> touchEvents) {
		paint2.setTextSize(30);
		if (touchEvents.size() > 0){
			Log.w("Debuggin", "Game is now running");
			engine.state = GameState.Running;
		}
		
	}

	private void updateRunning(List<TouchEvent> touchEvents, double deltaTime) {
		int len =  touchEvents.size();
		
		ArrayList<DragEvent> dragEvents = new ArrayList<DragEvent>();
		// Change this to be a class member instead of function variable.  
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_DOWN){
				engine.tryTouch(event); /// We check if this touch affects a ball.
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
			}
		}		
		// We have checked all our touch events.
		engine.collideDragEvents(dragEvents, deltaTime);
		// Moves all balls forward in time and checks for collisions.
		engine.updateBalls(deltaTime);
	}

	private void updateGameOver(List<TouchEvent> touchEvents){
		//
		
	}
	
	@Override
	public void paint(float deltaTime) {
		switch (engine.state) {
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

		g.drawString("Click to begin", gameWidth/2, gameHeight/2, paint);
	}

	/// Returns the current highscore. If there is no highscore, we create it and set it to 0.
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

	/// Checks if the current score is higher
	private boolean checkHighScore(){
		if (engine.score > highScore){
			Editor edit = settings.edit();
			edit.putInt("highScore", engine.score);
			edit.commit();
			return true;
		}
		return false;
	}

	
	private void drawRunningUI() {
		AndroidGraphics g = (AndroidGraphics) game.getGraphics();
		g.clearScreen(0);
		g.drawString("Score : " + engine.score, gameWidth - 400, 50, paint2);
		g.drawString("Lives : ", 80, 50, paint2);
		// Try to draw a small ball at this spot instead.
		
		int smallBallSize = (int)(testBall.getSize()*livesIndFactor);
		int livesXPos = 130; /// Should change this to be something variable to proportion probably.
		int livesYPos = 50 - (3*smallBallSize/4) ;
		for(int i = 0; i < engine.livesleft ; i++){
			g.drawScaledImage(Assets.ball, livesXPos, livesYPos , livesIndFactor);
			livesXPos += smallBallSize  * 1.3;
		}
		
		g.drawString("Highscore : " + highScore, gameWidth - 200, 50, paint2);

		ArrayList<Ball> balls = (ArrayList<Ball>) engine.balls;
		for (int i = 0; i < balls.size(); i++) {
			double ballSize = balls.get(0).getSize();		
			int ballX = (int) (balls.get(i).getX() - (ballSize/2));
			int ballY = (int) (balls.get(i).getY() - (ballSize/2));
			double angle = balls.get(i).getAngle();
			//g.drawImage(Assets.ball, ballX, ballY);
			g.drawRotatedScaledImage(Assets.ball, ballX, ballY, 1, angle);
		}
		TennisBall tennisball = engine.tennisball;
		if(tennisball != null){
			double ballSize = tennisball.getSize();
			int ballX = (int) (tennisball.getX() - (ballSize/2));
			int ballY = (int) (tennisball.getY() - (ballSize/2));
			g.drawImage(Assets.tennisball, ballX, ballY);
		}
	}

	
    private void drawGameOverUI() {
    	boolean isHighScore = checkHighScore();
    	game.setScreen(new EndScreen(game, engine.score, isHighScore));
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
