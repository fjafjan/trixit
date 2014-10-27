package com.trixit.framework.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.util.DisplayMetrics;


import com.trixit.framework.Audio;
import com.trixit.framework.FileIO;
import com.trixit.framework.Game;
import com.trixit.framework.Graphics;
import com.trixit.framework.Input;
import com.trixit.framework.Screen;

public abstract class AndroidGame extends Activity implements Game{
	AndroidFastRenderView renderView;
    Graphics graphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Removes the title bar since we don't want dat
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // gets full screen, I wonder why it isn't full screen to begin wtih?
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
        	      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                	WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Checks if the display is currently in portrait or landscape view and adjusts the frame size accordingly.
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        int frameBufferWidth = isPortrait ? 800: 1280; // ooh fancy one line conditional statements.
        int frameBufferHeight = isPortrait ? 1280: 800;
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
                frameBufferHeight, Config.RGB_565);

     // The scaling contents to scale our frames to the resolution of the screen.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float scaleX = (float) frameBufferWidth / metrics.widthPixels;
        float scaleY = (float) frameBufferHeight / metrics.heightPixels;       
        
        // We create all the interfaces defined by the game framework.
        renderView = new AndroidFastRenderView(this, frameBuffer);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);
        fileIO = new AndroidFileIO(this);
        audio = new AndroidAudio(this);
        input = new AndroidInput(this, renderView, scaleX, scaleY);
        screen = getInitScreen();
        
        // Get the saved settings (including things like high score etc)
        settings = getPreferences(0);

        // Set the screen to show our renderer.
        setContentView(renderView);
       
        // We create a wakelock so that the phone doesn't go to sleep while playing our lovely game.
//        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//      wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyGame");
    }

    // Called after another application has been started but our application has not been stopped.
    @Override
    public void onResume() {
        super.onResume();
        screen.resume();
        renderView.resume();
    }

    // When another application is starting and we think our game is gonna get shut down. 
    @Override
    public void onPause() {
        super.onPause();
        renderView.pause();
        screen.pause();

        if (isFinishing())
            screen.dispose();
    }

    // Functions to return our various interfaces. Unclear why this is needed...
    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public FileIO getFileIO() {
        return fileIO;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public void setScreen(Screen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }
   
    public Screen getCurrentScreen() {
        return screen;
    }

	@Override
	public Screen getInitScreen() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SharedPreferences getSettings(){
		return settings;
	}
}
