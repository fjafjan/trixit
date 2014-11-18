package com.trixit.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class DownloadHighScores extends AsyncTask<Void, Void, String>{

	InputStream is  = null;
	@Override
	protected String doInBackground(Void... params) {
		// TODO Auto-generated method stub
		try{
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://bengan1.se/fjafjan.php");
			//x List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>;
			
			
			URL url = new URL("http://bengan1.se");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(15000);
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			
			connection.connect();
			int response = connection.getResponseCode();
			Log.w("Debuggin", "We get the response" + response);
			is = connection.getInputStream();
		}catch(UnsupportedEncodingException e){
			return "Something was wrong with the encoding";
		}catch(IOException e){
			return "Unable to connect to TBengts server.";
		}

		return null;
	}
}
