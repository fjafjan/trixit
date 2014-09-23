package com.trixit.framework.implementation;

import android.media.SoundPool;

import com.trixit.framework.Sound;

public class AndroidSound implements Sound {
	int soundId;
	SoundPool soundPool;

	public AndroidSound(SoundPool soundPool, int soundId){
		this.soundPool = soundPool;
		this.soundId = soundId;
	}
	// Simply plays the sound with no balance changes, no looping and no speedup
	@Override
	public void play(float volume) {
		soundPool.play(soundId, volume, volume, 0, 0, 1);
	}
	
	// In some sense this removes the soundId from memory, but the whole point of the soundPool 
	// is to keep sounds in memory? 
	@Override
	public void dispose() {
		soundPool.unload(soundId);
	}

}
