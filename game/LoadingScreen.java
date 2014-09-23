package com.trixit.game;

import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Graphics.ImageFormat;
import com.trixit.framework.Screen;

public class LoadingScreen extends Screen{

	public LoadingScreen(Game game) {
		super(game);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(float deltaTime) {
		// Add assets here as I decide what i want to actually do.
		Graphics g = game.getGraphics();
		Assets.menu = g.newImage("manu.png", ImageFormat.RGB565);
		
		// We are done loading and start the game
		game.setScreen(new MainScreen(game));
	}

	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
		g.drawImage(Assets.menu, 0, 0);
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
