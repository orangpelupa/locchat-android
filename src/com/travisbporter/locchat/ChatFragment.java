package com.travisbporter.locchat;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.travisbporter.locchat.LocationMan;
import com.travisbporter.locchat.json.ChatMessage;
import com.travisbporter.locchat.json.ChatMessages;
import com.travisbporter.locchat.json.ChatMessagesRequest;
import com.travisbporter.locchat.linx.LinxUploadTask;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//Handles the main chat screen, has gotten bloated
//TODO: move things out of thise class

public class ChatFragment extends Fragment implements LocationMan.LocationChangeListener, WaveListener.WaveDetectListener, LinxUploadTask.LinxUploadListener{
	
	private AppPref appPref_;
	private Button sendMsg_;
	private Button sendPic_;
	private EditText msg_;
	private TextView chatBox_;
	private Location loc_;
	
	private Uri capUri_;
	private boolean capping_;
	
	public ChatFragment() {
		waveTime_ = System.currentTimeMillis();
		capping_ = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_main_chat,
				container, false);

		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle saved){
		super.onActivityCreated(saved);
		appPref_ = new AppPref(getActivity().getApplicationContext());
		
		msg_ = (EditText) getActivity().findViewById(R.id.editText_send);
		
		sendMsg_ = (Button) getActivity().findViewById(R.id.button_send);
		sendMsg_.setOnClickListener(sendMsgListener_);
		
		//We don't want to be able to send until we have a location
		if(loc_ == null){
			sendMsg_.setEnabled(false);
		}
		
		chatBox_ = (TextView) getActivity().findViewById(R.id.textView_chatWindow);
		
		//start polling for chat messages
		pollHandler_.post(pollRunner_);
		
		LocationMan locationListener = LocationMan.getInstance(getActivity().getApplicationContext(), this);
		locationListener.start();
		
		WaveListener waveListener = WaveListener.getInstance(getActivity(), this);
		if(Boolean.valueOf(appPref_.getPref(AppPref.KEY_PREFS_WAVE))){
			WaveListener.start();
		}
		
		sendPic_ = (Button) getActivity().findViewById(R.id.button_sendPic);
		sendPic_.setOnClickListener(sendPicListener_);
	}
	
	private OnClickListener sendMsgListener_ = new OnClickListener(){
		@Override
		public void onClick(View v) {
			sendMsg(msg_.getText().toString());
			msg_.setText("");
		}
		
	};
	
	
	//Starts a camera intent for getting a picture
	private OnClickListener sendPicListener_ = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
			capUri_ = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                    "locchat_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
            
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, capUri_);               
            cameraIntent.putExtra("return-data", true);
            startActivityForResult(cameraIntent, 1888);
			capping_ = true;
			sendPic_.setEnabled(!capping_);
		}

	};
	
	//helper that removes meta data from jpgs
	public static void exifStripper(ExifInterface e) throws IOException{
		e.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, "");
		e.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, "");
		e.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, "");
		e.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "");
		e.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "");
		e.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "");
		e.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "");
		e.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, "");
		e.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, "");
		e.saveAttributes();
	}
	
	//Poll for GetMsgTask
	private Handler pollHandler_ = new Handler();
	final Runnable pollRunner_ = new Runnable(){
		public void run(){
			new GetMsgTask().execute(appPref_.getPref(AppPref.KEY_PREFS_SERVER_URL)+"poll");
			pollHandler_.postDelayed(this, Integer.valueOf(appPref_.getPref(AppPref.KEY_PREFS_POLL))*1000);
		}
	};
	
	//Task for retrieving messages, handles throwing messages in a buffer
	private class GetMsgTask extends AsyncTask<String, String, ChatMessages>{

		@Override
		protected ChatMessages doInBackground(String... arg) {
			ChatMessages messages = null;
			
			HttpPost httpPost = new HttpPost(arg[0]);
			
			ChatMessagesRequest obj = new ChatMessagesRequest();
			obj.usr = appPref_.getPref(AppPref.KEY_PREFS_USER_NAME);
			if(loc_ != null){
				obj.lat = loc_.getLatitude();
				obj.lon = loc_.getLongitude();
			}
			obj.dist = Integer.valueOf(appPref_.getPref(AppPref.KEY_PREFS_DIST));
			
			Gson gson = new Gson();
			HttpResponse response = sendHtml(gson.toJson(obj),httpPost);
			
			if(response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				Reader reader;
				try {
					reader = new InputStreamReader(response.getEntity().getContent());
					messages = gson.fromJson(reader,ChatMessages.class);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return messages;
		}
		
		//TODO: our message buffer shouldn't just be a long string!
		@Override
	    protected void onPostExecute(ChatMessages messages) {
			if(messages != null){
				if(messages.msgs != null){
					String cat = "";
					for(ChatMessage m : messages.msgs){
						cat = '\n' + m.usr + ": " + m.msg + cat;
					}
					chatBox_.setText(cat);
				} else {
					chatBox_.setText("");
				}
			}
	    }
	}
	
	//Task for sending messages, http posts a JSON
	private class SendMsgTask extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... arg) {

			HttpPost httpPost = new HttpPost(arg[0]);
			ChatMessage obj = new ChatMessage();
			obj.usr = appPref_.getPref(AppPref.KEY_PREFS_USER_NAME);
			obj.msg = arg[1];
			obj.time = System.currentTimeMillis()/1000L;
			if(loc_ != null){
				obj.lat = loc_.getLatitude();
				obj.lon = loc_.getLongitude();
			}
			Gson gson = new Gson();
			HttpResponse response = sendHtml(gson.toJson(obj),httpPost);

			return null;
		}
		
	}
	
	private static HttpResponse sendHtml(String s, HttpPost httpPost){
		HttpResponse response = null;
		HttpClient httpclient = new DefaultHttpClient();
		StringEntity se;
		try {
			se = new StringEntity(s);
			httpPost.setEntity(se);
			httpPost.setHeader("Accept","application/json");
			httpPost.setHeader("Content-type","application/json");
			response = httpclient.execute(httpPost);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public void onLocationChange(Location l) {
		loc_ = l;
		sendMsg_.setEnabled(true); // we have a location, allow user to send message
	}

	
	private long waveTime_;
	@Override
	public void onWaveDetected() {
		//Only count waves 5 seconds apart
		if((System.currentTimeMillis() - waveTime_)>5000){
			waveTime_ = System.currentTimeMillis();
			sendMsg("WAVE");
		}
	}
	
	@Override
	public void onLinxUploadFinish(String url) {
		sendMsg("Pic uploaded: " + url);
		capping_ = false;
		sendPic_.setEnabled(!capping_);
	}
	
	//Adds message to local chat then sends message off to the server
	private void sendMsg(String msg){
		chatBox_.setText(chatBox_.getText().toString() +'\n' + appPref_.getPref(AppPref.KEY_PREFS_USER_NAME) + ": " + msg);
		new SendMsgTask().execute(appPref_.getPref(AppPref.KEY_PREFS_SERVER_URL)+"chat",msg);
	}

	//Used for after a picture is taken
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == 1888){ 
			try {
				exifStripper(new ExifInterface(capUri_.getPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			new LinxUploadTask(this).execute(capUri_);
		}
	}
}
