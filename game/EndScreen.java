package com.trixit.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Screen;
import com.trixit.framework.Input.TouchEvent;

public class EndScreen extends Screen{

	Paint paint;
	Random random = new Random();

	int score;
	int gameWidth, gameHeight;
	String grade, review;
	double timeAlive, waitTime;
	boolean isHighScore;
	
	public EndScreen(Game game, int score, boolean isHighScore) {
		super(game);
		this.score = score;
		this.isHighScore = isHighScore;
		
		this.gameWidth = game.getGraphics().getWidth();
		this.gameHeight = game.getGraphics().getHeight();
		
		this.grade = getGrade(score);
		this.review = getReview(grade);
		
		this.timeAlive = 0;      /// The time since this endScreen first appeared
		this.waitTime = 100;    /// The minimum amount of time the user has to wait before starting a new game.
		
		paint = new Paint();
		paint.setTextSize(30);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
	}

	@Override
	public void update(float deltaTime) {
		
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		timeAlive += deltaTime;
		if(timeAlive < waitTime)
			return;
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
        Graphics g = game.getGraphics();
        paint.setTextSize(100);
        g.drawRect(0, 0, gameWidth+1, gameHeight+1, Color.BLACK);
        g.drawString("GAME OVER.", gameWidth/2, gameHeight/2 - 350, paint);
        g.drawString("Grade:", gameWidth/2, gameHeight/2 - 150, paint);
        paint.setTextSize(300);
        g.drawString(grade, gameWidth/2, gameHeight/2 + 150, paint);
        paint.setTextSize(50);
        drawLongString(review, gameWidth/2, gameHeight/2 + 300, 50);
        
        if(this.isHighScore){
        	String congratulations = "Congratulations! \n Your new high score is: " + score;
        	drawLongString(congratulations , gameWidth/2, gameHeight - 100, 50);
        }else{
        	String scoreStr = "Your score was " + score;
        	drawLongString(scoreStr , gameWidth/2, gameHeight - 100, 50);
        }
	}

    private String getGrade(int score){
    	String grade = "Cheater";
    	if(score == 0){
    		grade = "F--" ;
    	}else if(score < 5){
    		grade = "F";
    	}else if(score < 10){
    		grade = "E";
    	}else if(score < 20){
    		grade = "D";
    	}else if(score < 30){
    		grade = "C";
    	}else if(score < 40){
    		grade = "B";
    	}else if(score < 50){
    		grade = "A";
    	}else if(score < 70){
    		grade = "A++";
    	}else{
    		grade = "Z";
    	}
    	return grade;
    }
    
    private String getReview(String grade){
    	ArrayList<String> reviews = new ArrayList<String>();
    	String review = "";
    	if (grade == "F--"){
    		reviews.add("We're all very sorry");
    		reviews.add("Whelp");
    		reviews.add("Now let's have a try from someone \n who's not a complete [redacted]");
    		reviews.add("Okay, now you try!");
    	}else if (grade == "F"){
			reviews.add("Hopefully you're a good person");
			reviews.add("Next time use your fingers");
			reviews.add("Call 911");
			reviews.add("Straight up very bad");
			reviews.add("I hope this was your first try");
			reviews.add("I will need to speak with \n your parent or guardian");
			reviews.add("Maybe it's time you move ");
			reviews.add("F stands for Failure");
    	}else if (grade == "E"){
    		reviews.add("Still very bad");
			reviews.add("You have to do better \n  than that...");
			reviews.add("I told you you would fail");
			reviews.add("You are a shame \n  to your family");
			reviews.add("This game may not be for you");
			reviews.add("You suck"); 
			reviews.add("I hope no one saw that");
			reviews.add("E stands for Embarrassing");
    	}else if (grade == "D"){
    		reviews.add("You are the very best \n  of failures!");
			reviews.add("Impressive... for a monkey");
			reviews.add("You almost don't suck!");
			reviews.add("I hope you can do better");
			reviews.add("You are not gifted");
			reviews.add("Almost acceptable");
			reviews.add("You should lie \n about this result");
			reviews.add("D stands for Dumb");
    	}else if (grade == "C"){
    		reviews.add("You are mediocre!");
			reviews.add("Meh");
			reviews.add("Okay, now do that again");
			reviews.add("You're not bad!");
			reviews.add("Acceptable");
			reviews.add("Not disappointing");
			reviews.add("You achieved the bare minimum");
			reviews.add("C stands for ... \n I don't even know");
    	}else if (grade == "B"){
    		reviews.add("You did okay");
    		reviews.add("A decent performance!");
    		reviews.add("Maybe there is some potential \n in you after all");
    		reviews.add("Not an embarrasing result");
    		reviews.add("You shouldn't really be proud \n of it");
    		reviews.add("B stands for Barely good");
    	}else if (grade == "A"){
    		reviews.add("You got some moves");
    		reviews.add("You could be an olympic \n time waster!");
    		reviews.add("You can now move on with \n your 'life'");
    		reviews.add("What do you want? A gold star?");
    		reviews.add("Good job!");
    		reviews.add("A stands for awesome");
    	}else if (grade == "A++"){
    		reviews.add("Are you a footballer?");
    		reviews.add("This is getting impressive now");
    		reviews.add("You are probably cheating \n in some way");
    		reviews.add("Your successes as a gamer \n  are my failures as a developer");
    		reviews.add("Is this the real life\nis this just fantsy?");
    		reviews.add("Could I make a game so hard \n  that you could not beat it?");
    		reviews.add("Woah!");
    	}else if (grade == "Z"){
    		reviews.add("You are Zlatan.");
    	}else{
    		reviews.add("You're pretty good!");
		}
    	
    	int randomIndex = random.nextInt(reviews.size()); 
    	review = reviews.get(randomIndex);
    	return review;
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
		nullify();
	}

	@Override
	public void backButton() {
		nullify();		
	}
	
	private void drawLongString(String s, int xPos, int yPos, int textSize){
		int maxLength = 30;
		Graphics g = game.getGraphics();
		
		// If there are no newlines.
		if(s.indexOf("\n") == -1){
			String remaining = s;
			int currY = yPos;
			while(remaining.length() > maxLength){
				String piece = remaining.substring(0, maxLength);
				remaining = remaining.substring(maxLength, remaining.length());
				g.drawString(piece, xPos , currY , paint);
				currY += textSize*1.5;
			}
			g.drawString(remaining, xPos , currY , paint);
		}else{		
			ArrayList<String> lines = new ArrayList<String>();
			String remaining = s;
			while(remaining.indexOf("\n") != -1){
				lines.add(remaining.substring(0,s.indexOf("\n") + 1 ));
				remaining = remaining.substring(s.indexOf("\n") + 1, remaining.length());
			}
			lines.add(remaining);
			int currY = yPos;
			for (int i = 0; i < lines.size(); i++) {
				g.drawString(lines.get(i), xPos , currY , paint);
				currY += textSize*1.5;
			}
		}
	}
	
	private void nullify() {
		paint = null;
		System.gc();
	}
	
}
