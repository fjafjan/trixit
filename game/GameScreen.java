package com.trixit.game;




import java.util.List;
import java.util.ArrayList;

import android.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	PackageManager manager;
	
	Ball testBall;
	double livesIndFactor, dragSpeed;
	int gameHeight, gameWidth, highScore;
	float volume;
	/// debuggin variables
	double averageFPS = 0;

    Finger[] fingers = new Finger[4];
	
	public GameScreen(Game game){
		super(game);
		livesIndFactor = 0.4;	/// How much smaller the little balls indicating lives left are.		
		dragSpeed = 0.1;
		
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
		String version = getVersion();
		if (checkVersion(version)){					/// If we have a new version
			setHighScore(0);						/// Sets the highscore to 0 
		}
		
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
		/// TEMPORARY
		
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
		
		// Add new touch events to each finger
		updateFingerList(touchEvents, deltaTime);
		
		// For each finger, update the position and velocity. 
		for(int i = 0 ; i < fingers.length ; i++){
			if(fingers[i] == null){
				continue;
			}
			fingers[i].updateFinger(deltaTime);
//			Log.w("Debuggin", "The current finger velocity for finger " + i + " is " + fingers[i].vel.length());
			if(fingers[i].vel.length() < dragSpeed){
				engine.tryTouch(fingers[i].pos);
			}else{ // else we try dragging
				engine.tryDrag(fingers[i], deltaTime);
			}
		}		
		
		// Removes any fingers that have been lifted. 
		cleanFingers();
		
		engine.updateBalls(deltaTime);
	}

	private void updateFingerList(List<TouchEvent> touchEvents, double deltaTime) {
		int len =  touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			int id = event.pointer;
			if (event.type == TouchEvent.TOUCH_DOWN){
				if (fingers[id] != null)
					Log.w("Debuggin", "Duplicate fingers in da hizzous");
//					throw new RuntimeException("Duplicate fingers in da hizzous");
				fingers[id] =new Finger(event);
				fingers[id].addEvent(event);
			}else{
				if (fingers[id] == null){
					Log.w("Debuggin", "Finger " + id + " was somehow not touched down ever. Weird!");
					fingers[id] = new Finger(event);
				}
				if(event.type == TouchEvent.TOUCH_DRAGGED){
					fingers[id].addEvent(event);
				}else if(event.type == TouchEvent.TOUCH_UP){
					fingers[id].destroy = true;
				}
			}
		}
		
	}

	private void cleanFingers(){
		for (int i = 0; i < fingers.length; i++) {
			if(fingers[i] != null){
				if (fingers[i].destroy){
					fingers[i] = null;
				}
			}
		}
	}
	

	private void updateGameOver(List<TouchEvent> touchEvents){
		
	}
	
	
	@Override
	public void paint(float deltaTime) {
		switch (engine.state) {
		case Ready: 
			drawReadyUI();
			break;
		case Running:
			drawRunningUI();
			//printFPS(deltaTime);
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

	/// Given the current String version of the version of the game 
	/// we check if the version has changed since last we ran. If so returns true
	private boolean checkVersion(String version){
		if(settings.contains("version")){
			if(settings.getString("version", "").equals(version)){
				return false;
			}
		}
		// Either the version if our of date, or we have never set it before. 
		Editor edit = settings.edit();
		edit.putString("version", version);
		edit.commit();
		return true;
	}
	
	/// Returns the current high score. If there is no high score, we create it and set it to 0.
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

	/// Checks if the current score is higher. If so it sets
	/// the current high score to that score. 
	private boolean checkHighScore(){
		if (engine.score > highScore){
			setHighScore(engine.score);
			return true;
		}
		return false;
	}

	private void setHighScore(int score){	
		Editor edit = settings.edit();
		edit.putInt("highScore", score);
		edit.commit();
	}

	
	
	private void drawRunningUI() {
		AndroidGraphics g = (AndroidGraphics) game.getGraphics();
		g.clearScreen(0);
		int backGroundColor = Color.argb(255, 25, 25, 25);
		g.drawRect(0, 0, gameWidth+1, gameHeight+1, backGroundColor);
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
		
		g.drawString("High score : " + highScore, gameWidth - 200, 50, paint2);
		
		/// We draw all the regular balls.
		ArrayList<Ball> balls = (ArrayList<Ball>) engine.balls;
		for (int i = 0; i < balls.size(); i++) {
			double ballSize = balls.get(0).getSize();		
			int ballX = (int) (balls.get(i).getX() - (ballSize/2));
			int ballY = (int) (balls.get(i).getY() - (ballSize/2));
			double angle = balls.get(i).getAngle();
			g.drawRotatedScaledImage(Assets.ball, ballX, ballY, 1, angle);
		}
		
		/// We draw the tennisball (atm only one). 
		TennisBall tennisball = engine.tennisball;
		if(tennisball != null){
			double ballSize = tennisball.getSize();
			int ballX = (int) (tennisball.getX() - (ballSize/2));
			int ballY = (int) (tennisball.getY() - (ballSize/2));
			double angle = tennisball.getAngle();
			///g.drawImage(Assets.tennisball, ballX, ballY);
			g.drawRotatedScaledImage(Assets.tennisball, ballX, ballY, 1, angle);
			}
	}

	
    private void drawGameOverUI() {
    	boolean isHighScore = checkHighScore();
    	
    	// Move everything below to a new method called something like
    	// get global highscore or something. 
//    	Context context = (Context) game;
 //   	ConnectivityManager connMgr = (ConnectivityManager) 
  //  			context.getSystemService(Context.CONNECTIVITY_SERVICE);
   // 	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
   // 	if(networkInfo != null && networkInfo.isConnected()){
    		
    		// Try to connect to Tbengts server and download that shit. 
   //	}else{
    		// say something about network being unavailable and only a local 
    		// or old high score list is available. 
   // 	}
    	game.setScreen(new EndScreen(game, engine.score, isHighScore));
    }
    
    private void printFPS(float deltaTime){
    	double FPS = 1000/deltaTime;
    	Graphics g = game.getGraphics();
    	g.drawString("current FPS: " + FPS, gameWidth - 200, gameHeight - 50, paint2);
    }
    
    
    /// Return the current version number of this game. 
    private String getVersion(){
    	String version = "";

    	try {
    		Context context = (Context) game;
    		PackageManager manager = context.getPackageManager();
    	    PackageInfo info = manager.getPackageInfo(
    	    context.getPackageName(), 0);
    	    version = info.versionName;
    	} catch (Exception e) {
    		Log.w("Debuggin", "Error getting version");
    		return "";
    	}
    	

    	Log.w("Debuggin", "Game version is " + version);
    	return version;
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
