package com.trixit.game;

import android.util.Log;

import com.trixit.framework.Sound;
import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Screen;
import com.trixit.framework.Graphics.ImageFormat;

// This is the loading screen that starts the game. 
public class SplashLoadingScreen extends Screen {

	public SplashLoadingScreen(Game game){
		super(game);
	}
	@Override
	public void update(float deltaTime) {
		Log.w("Debuggin", "We get to SplashScreen");

		
		Graphics g = game.getGraphics();
		// Load images
		Assets.menuPortrait = g.newImage("menu_portrait.jpg", ImageFormat.RGB565);
		Assets.menuLandscape = g.newImage("menu_landscape.jpg", ImageFormat.RGB565);
		Assets.ball = g.newImage("ball2.png", ImageFormat.ARGB4444);
		Assets.tennisball = g.newImage("tennisball.png", ImageFormat.ARGB4444);
		
		// Load sounds
		Assets.click = game.getAudio().createSound("explosion.ogg");
		Assets.kick = game.getAudio().createSound("kick-1.wav");
		
		int nrOfKickSounds = 5;
		Assets.kicks = new Sound[nrOfKickSounds];
		for (int i = 0; i < nrOfKickSounds; i++) {
			Assets.kicks[i] = game.getAudio().createSound("kick-"+(i+1)+".wav");
		}
		
		int nrOfBounceSounds = 1;
		Assets.bounces = new Sound[nrOfBounceSounds];
		for (int i = 0; i < nrOfBounceSounds; i++) {
			Assets.bounces[i] = game.getAudio().createSound("ground-"+(i+1)+".wav");
		}
		
		game.setScreen(new MainScreen(game));
	}

	@Override
	public void paint(float deltaTime) {	

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
		// TODO Auto-generated method stub

	}
}
