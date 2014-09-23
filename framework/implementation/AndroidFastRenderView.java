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
			
			float deltaTime = (System.nanoTime() - startTime) / 10000000.000f;
			startTime = System.nanoTime();
			
			// If it's too slow then .. we set it to something? 
			// This whole thing is used to base movement and activity
			// on actual time as opposed to framerate, as each iteration
			// of this loop can occur arbitrarily quickly or slowly.
			// The limit of 3.15 is to prevent things for going too slowly.
			if (deltaTime > 3.15){
				deltaTime = (float) 3.15;
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
