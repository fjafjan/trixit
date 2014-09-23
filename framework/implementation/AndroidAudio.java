package com.trixit.framework.implementation;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

import com.trixit.framework.Audio;
import com.trixit.framework.Music;
import com.trixit.framework.Sound;


public class AndroidAudio implements Audio{
	AssetManager assets;
	SoundPool soundPool;
	
	public AndroidAudio(Activity activity){
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		this.assets = activity.getAssets();
		this.soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
	}
	@Override
	public Music createMusic(String fileName) {
		try{
			AssetFileDescriptor assetDescriptor = assets.openFd(fileName);
			return new AndroidMusic(assetDescriptor);
		}catch(IOException e){
			throw new RuntimeException("We were unable to load the music " + 
		fileName + " :(");
		}
	}

	@Override
	public Sound createSound(String fileName) {
		try{
			AssetFileDescriptor assetDescriptor = assets.openFd(fileName);
			int soundId = soundPool.load(assetDescriptor,0);
			return new AndroidSound(soundPool, soundId);
		}catch(IOException e){
			throw new RuntimeException("We were unable to load a sound we + " +
					fileName + " :(");
		}
	}
	
}
