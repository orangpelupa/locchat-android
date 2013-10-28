package com.travisbporter.locchat.linx;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.travisbporter.locchat.linx.json.Response;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class LinxUploadTask extends AsyncTask<Uri, Integer, String> {
	public static final String TAG = "Linx";
	
	LinxUploadListener listener_;
    public interface LinxUploadListener {
        public void onLinxUploadFinish(String url);
    }
    
    public LinxUploadTask(LinxUploadListener l){
    	listener_ = l;
    }
    
	@Override
	protected String doInBackground(Uri... params) {
		Log.d(TAG, "Started");
		HttpResponse response = null;
		String retVal = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut("https://linx.li/upload/public/");
		httpPut.setHeader("X-Randomized-Barename", "yes");
		httpPut.setHeader("Accept","application/json");
		
		Log.d(TAG, "Sending" + params[0].getPath());
		
		FileEntity fe = new FileEntity(new File(params[0].getPath()),"binary/octet-stream");
		httpPut.setEntity(fe);
		try {
			response = httpClient.execute(httpPut);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
			Reader reader;
			try {
				reader = new InputStreamReader(response.getEntity().getContent());
				Gson gson = new Gson();
				Response res = gson.fromJson(reader,Response.class);
				retVal = res.urlShort;
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.d(TAG, "Finished with retVal: " + retVal);
		return retVal;
	}
	@Override
	protected void onPostExecute(String url){
		listener_.onLinxUploadFinish(url);
	}

}
