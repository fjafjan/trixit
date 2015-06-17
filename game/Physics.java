package com.trixit.game;

import java.util.ArrayList;

import com.trixit.framework.Vector2d;

public class Physics {
	Options options;
	
	public Physics(Options options){
		this.options = options;
	}
	
	public void updateBalls(ArrayList<Ball> balls, double deltaTime){
		
		/// Update all regular balls
		for(int i=0 ; i < balls.size() ; i++){
			balls.get(i).update(deltaT);
			Vector2d pos = balls.get(i).getPos();
			// We check for collisions
			Ball collidedWith = inBall(pos, balls.get(i));
			balls.get(i).collide(collidedWith);

			/// Check if a ball hits a wall.
			checkEdges(balls.get(i));			
		}

		/// If we have a tennisball, update that too.
		if(tennisball != null){
			tennisball.update(deltaT);
			checkEdges(tennisball);
			if(tennisball.destroy)
				tennisball = null;
		}
	}

	private int inBall(double x, double y, double radius){
		// I should replace ball with "spheroid g object" for the sake of everyone involved.
		// Check for all balls if this coordinate is .. within that.
		Vector2d pos = new Vector2d(x, y);

		
		// We check if there is a colission with the tennisball
		if(tennisball != null){
			double ballSize = tennisball.getSize();
			Vector2d ballPos = tennisball.getPos();
			if ( pos.diff(ballPos).length()  < ((ballSize/2)  + radius)){
				return -2;
			}
		}

		/// If not, then we check if there a collision with any of the other balls. 
		for (int i = 0; i < balls.size(); i++) {
			double ballSize = balls.get(i).getSize();
			Vector2d ballPos = balls.get(i).getPos();
			if ( pos.diff(ballPos).length()  < ((ballSize/2)  + radius)){
				return i;
			}
		}
		return -1;
	}

	
	
}
