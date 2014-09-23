package com.trixit.game;

import android.util.Log;

import com.trixit.framework.implementation.*;
import com.trixit.framework.Screen;

public class TrixitGame extends AndroidGame{
	@Override
	public Screen getInitScreen(){
		Log.w("error", "We get nowheres");
		return new SplashLoadingScreen(this);
	}
}
