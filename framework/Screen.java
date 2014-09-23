package com.trixit.framework;

// Handles graphical things I suppose, no idea how this will work. Is this guide too
// quick and dirty? We'll see, I think it's fine to create a skeleton first though.
public abstract class Screen {
    protected final Game game;

    public Screen(Game game) {
        this.game = game;
    }

    public abstract void update(float deltaTime);

    public abstract void paint(float deltaTime);

    public abstract void pause();

    public abstract void resume();

    public abstract void dispose();
   
    public abstract void backButton();
   
}
	