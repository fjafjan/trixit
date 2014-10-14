package com.trixit.game;

import java.util.List;

import com.trixit.framework.Screen;
import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Input.TouchEvent;

import android.util.Log;

public class MainScreen extends Screen {
	
	public MainScreen(Game game){
		super(game);
	}	

	@Override
	public void update(float deltaTime) {
		Graphics g = game.getGraphics();
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		
		int len = touchEvents.size();
		for(int i = 0 ; i < len ; i++){
			try{
				TouchEvent event = touchEvents.get(i);
				if (event.type == TouchEvent.TOUCH_DOWN){
					if (inBounds(event, 0, 0, g.getWidth(), g.getHeight())){ 
						// Start game
						game.setScreen(new GameScreen(game));
					}
				}
			}catch(IndexOutOfBoundsException e){
				Log.w("Debuggin", "We think we have " + len + " events but actually we have " + i );
				break;
			}
		}
	}
	
	private boolean inBounds(TouchEvent event, int x, int y, int width, int height){
		return (event.x > x && event.x < x + width) && 
				(event.y > y && event.y < y + height);
	}

	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
		if(g.getHeight() > g.getWidth()){ // portrait
			g.drawImage(Assets.menuPortrait, 0, 0);
		}else{
			g.drawImage(Assets.menuLandscape, 0, 0);
		}
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
	