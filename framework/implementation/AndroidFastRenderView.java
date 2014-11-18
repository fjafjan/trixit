package com.trixit.framework.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AndroidFastRenderView extends SurfaceView implements Runnable{
	AndroidGame game;
	Bitmap framebuffer;
	Thread renderThread = null;
	SurfaceHolder holder;
	// What does volatile and synchronized really mean?	
	volatile boolean running = false;

	
	/// TESTING STUFF BEGIN
	// avoid GC in your threads. declare nonprimitive variables out of onDraw
    float smoothedDeltaRealTime_ms=17.5f; // initial value, Optionally you can save the new computed value (will change with each hardware) in Preferences to optimize the first drawing frames 
    float movAverageDeltaTime_ms=smoothedDeltaRealTime_ms; // mov Average start with default value
    long lastRealTimeMeasurement_ms; // temporal storage for last time measurement

    // smooth constant elements to play with
    static final float movAveragePeriod=40; // #frames involved in average calc (suggested values 5-100)
    static final float smoothFactor=0.1f; // adjusting ratio (suggested values 0.01-0.5)

    /// TESTING STUFF END
	
	
	
	public AndroidFastRenderView(AndroidGame game, Bitmap framebuffer){
		super(game);
		this.game = game;
		this.framebuffer = framebuffer;
		this.holder = getHolder();
	}

	public void resume(){
		running = true;
		renderThread = new Thread(this);
		renderThread.start();
	}
	
	@Override
	public void run() {
		Rect dstRect = new Rect();
		long startTime = System.nanoTime();
		while(running){
			// This is weird? Is the surface is NOT valid we continue? 
			if(!holder.getSurface().isValid()){
				continue;
			}
			
			float deltaTime = (System.nanoTime() - startTime) / 1000000.000f;
			startTime = System.nanoTime();

			



	        // Moving average calc

//	        long currTimePick_ms= (long) startTime/1000000;
//	        float realTimeElapsed_ms;
//	        if (lastRealTimeMeasurement_ms>0){
//	        realTimeElapsed_ms=(currTimePick_ms - lastRealTimeMeasurement_ms);
//	        } else {
//	                 realTimeElapsed_ms=smoothedDeltaRealTime_ms; // just the first time
//	        }
//	        movAverageDeltaTime_ms=(realTimeElapsed_ms + movAverageDeltaTime_ms*(movAveragePeriod-1))/movAveragePeriod;

	         // Calc a better aproximation for smooth stepTime
//	        smoothedDeltaRealTime_ms=smoothedDeltaRealTime_ms +(movAverageDeltaTime_ms - smoothedDeltaRealTime_ms)* smoothFactor;

//	        lastRealTimeMeasurement_ms=currTimePick_ms;

//	        deltaTime = smoothedDeltaRealTime_ms;
			// If it's too slow then .. we set it to something? 
			// This whole thing is used to base movement and activity
			// on actual time as opposed to framerate, as each iteration
			// of this loop can occur arbitrarily quickly or slowly.
			// The limit of 31.5 is to prevent things for going too slowly.
	        //deltaTime = (System.nanoTime() - startTime) / 1000000.000f;
			if (deltaTime > 31.5){
				deltaTime = (float) 31.5;
			}
			
			game.getCurrentScreen().update(deltaTime);
			game.getCurrentScreen().paint(deltaTime);
			
			Canvas canvas = holder.lockCanvas();
			// What the heck does this do? 
			canvas.getClipBounds(dstRect);
			canvas.drawBitmap(framebuffer, null, dstRect, null);
			holder.unlockCanvasAndPost(canvas);
		}
	}
	
	
	public void pause(){
		running = false;
		while(true){
			try{
				renderThread.join();
				break;
			}catch(InterruptedException e){
				// We're not allowed to resume yet
			}
		}
	}
}
