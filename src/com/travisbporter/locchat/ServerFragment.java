package com.travisbporter.locchat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//Server settings fragment
public class ServerFragment extends Fragment {
	
	private AppPref appPref_;
	private Button apply_;
	private EditText serverURL_;
	private EditText poll_;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_server,
				container, false);


		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle saved){
		super.onActivityCreated(saved);
		appPref_ = new AppPref(getActivity().getApplicationContext());
		
		apply_ = (Button) getActivity().findViewById(R.id.button_applyServer);
		apply_.setOnClickListener(applyListener_);
		
		serverURL_ = (EditText) getActivity().findViewById(R.id.editText_serverAddress);
		serverURL_.setText(appPref_.getPref(AppPref.KEY_PREFS_SERVER_URL));
		
		poll_ = (EditText) getActivity().findViewById(R.id.editText_pollTime);
		poll_.setText(appPref_.getPref(AppPref.KEY_PREFS_POLL));
	}
	
	
	private OnClickListener applyListener_ = new OnClickListener(){
		@Override
		public void onClick(View v) {
			appPref_.savePref(AppPref.KEY_PREFS_SERVER_URL, serverURL_.getText().toString());
			if(Integer.valueOf(poll_.getText().toString())>=1){
				appPref_.savePref(AppPref.KEY_PREFS_POLL, poll_.getText().toString());
			}
			Toast t = Toast.makeText(getActivity().getApplicationContext(), "Applied.", Toast.LENGTH_SHORT);
			t.show();
		}
		
	};
}
