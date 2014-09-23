package com.trixit.framework.implementation;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;

import com.trixit.framework.Music;

public class AndroidMusic implements Music, OnCompletionListener, OnSeekCompleteListener, OnPreparedListener,
OnVideoSizeChangedListener{

	MediaPlayer mediaPlayer;
	boolean isPrepared = false;
	
	public AndroidMusic(AssetFileDescriptor assetDescriptor){
		mediaPlayer = new MediaPlayer();
		try{
			mediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(), 
					assetDescriptor.getStartOffset(),
					assetDescriptor.getLength());
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnSeekCompleteListener(this);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnVideoSizeChangedListener(this);
		
		}catch(Exception e){
			throw new RuntimeException("Failed to initiate AndroidMusic");
		}
	}
	
	@Override
	public void play() {
			if (this.mediaPlayer.isPlaying())
				return;
			
			try{
				synchronized (this) {
					if (!isPrepared)
						mediaPlayer.prepare();
					mediaPlayer.start();
				}
			}catch(IllegalStateException e){
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
			
	}

	@Override
	public void stop() {
		if (this.mediaPlayer.isPlaying()){
			mediaPlayer.stop();
			
			synchronized (this){
				isPrepared = false;
			}
		}
	}

	@Override
	// Why do we need to specify this in the first case and not the second. 
	public void pause() {
		if (this.mediaPlayer.isPlaying())
			mediaPlayer.pause();
	}

	@Override
	public void setLooping(boolean looping) {
		mediaPlayer.setLooping(looping);

	}

	@Override
	public void setVolume(float volume) {
		mediaPlayer.setVolume(volume, volume);
	}

	@Override
	public boolean isPlaying() {
		return this.mediaPlayer.isPlaying();
	}

	@Override
	public boolean isStopped() {
		return !isPrepared;
	}

	@Override
	public boolean isLooping() {
		return this.mediaPlayer.isLooping();
	}

	@Override
	public void dispose() {
		if (this.mediaPlayer.isPlaying()){
			this.mediaPlayer.stop();
		}
		this.mediaPlayer.release();
	}

	// Go to the start of the music
	@Override
	public void seekBegin() {
		mediaPlayer.seekTo(0);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		synchronized (this){
			isPrepared = false;
		}		
		
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		synchronized (this){
			isPrepared = true;
		}
	}

	
	@Override
	public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}


}
