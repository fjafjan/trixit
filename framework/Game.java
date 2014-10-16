package com.trixit.framework;

import android.content.SharedPreferences;

// Let's see if this seems like a reasonable idea, I would say that it does. 
public interface Game {
	public Audio getAudio();

    public Input getInput();

    public FileIO getFileIO();

    public Graphics getGraphics();

    public void setScreen(Screen screen);

    public Screen getCurrentScreen();

    public Screen getInitScreen();
    
    public SharedPreferences getSettings();
}
