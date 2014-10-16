package com.trixit.game;

import android.graphics.Color;
import android.graphics.Paint;

import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Screen;

public class EndScreen extends Screen{

	int score;
	Paint paint;
	int gameWidth, gameHeight;
	
	public EndScreen(Game game, int score) {
		super(game);
		this.score = score;
		this.gameWidth = game.getGraphics().getWidth();
		this.gameHeight = game.getGraphics().getHeight();
		
		paint = new Paint();
		paint.setTextSize(30);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
	}

	@Override
	public void update(float deltaTime) {
    	String grade = getGrade(score);
    	String review = getReview(grade);
    	
        Graphics g = game.getGraphics();
        paint.setTextSize(100);
        g.drawRect(0, 0, gameWidth+1, gameHeight+1, Color.BLACK);
        g.drawString("GAME OVER.", gameWidth/2, gameHeight/2, paint);		
	}

	@Override
	public void paint(float deltaTime) {
		// TODO Auto-generated method stub
		
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

    private String getGrade(int score){
    	String grade = "Cheater";
    	if(score == 0){
    		grade = "F--" ;
    	}else if(score < 5){
    		grade = "F";
    	}else if(score < 10){
    		grade = "E";
    	}else if(score < 15){
    		grade = "D";
    	}
    	return grade;
    }
    
    private String getReview(String grade){
    	return "";
    }

	
}
