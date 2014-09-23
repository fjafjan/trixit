package com.trixit.game;

import android.util.Log;

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
		Assets.menu = g.newImage("menu.jpg", ImageFormat.RGB565);
		Assets.ball = g.newImage("ball.png", ImageFormat.RGB565);
		Assets.click = game.getAudio().createSound("explosion.ogg");
		
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
