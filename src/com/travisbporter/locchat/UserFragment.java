package com.travisbporter.locchat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

//User settings fragment
public class UserFragment extends Fragment {
	
	private AppPref appPref_;
	private Button apply_;
	private EditText userName_;
	private SeekBar dist_;
	private TextView distText_;
	private CheckBox wave_;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_user,
				container, false);


		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle saved){
		super.onActivityCreated(saved);
		appPref_ = new AppPref(getActivity().getApplicationContext());
		
		apply_ = (Button) getActivity().findViewById(R.id.button_userApply);
		apply_.setOnClickListener(applyListener_);
		
		userName_ = (EditText) getActivity().findViewById(R.id.EditText_userName);
		userName_.setText(appPref_.getPref(AppPref.KEY_PREFS_USER_NAME));
		
		dist_ = (SeekBar) getActivity().findViewById(R.id.seekBar_distance);
		dist_.setProgress(Integer.parseInt(appPref_.getPref(AppPref.KEY_PREFS_DIST)));
		dist_.setOnSeekBarChangeListener(distListener_);
		
		distText_ = (TextView) getActivity().findViewById(R.id.textView_distance);
		distText_.setText(String.valueOf(distCalc(dist_.getProgress()))+" mi.");
		
		wave_ = (CheckBox) getActivity().findViewById(R.id.checkBox_wave);
		wave_.setChecked(Boolean.valueOf(appPref_.getPref(AppPref.KEY_PREFS_WAVE)));
	}
	
	private int distCalc(int dist){
		if(dist<1)
			dist=1;
		//TODO: put this calculation somewhere else
		//Calculates a *very* rough estimate of the radius in miles
		//Also adds a curve instead of a linear change
		return (int) Math.ceil(Math.pow(dist/13F,3)*60F);
	}
	
	private OnClickListener applyListener_ = new OnClickListener(){
		@Override
		public void onClick(View v) {
			appPref_.savePref(AppPref.KEY_PREFS_USER_NAME, userName_.getText().toString());
			appPref_.savePref(AppPref.KEY_PREFS_DIST, String.valueOf(dist_.getProgress()));
			appPref_.savePref(AppPref.KEY_PREFS_WAVE, String.valueOf(wave_.isChecked()));
			
			
			if(wave_.isChecked()){
				WaveListener.start();
			}else {
				WaveListener.stop();
			}
			
			Toast t = Toast.makeText(getActivity().getApplicationContext(), "Applied.", Toast.LENGTH_SHORT);
			t.show();
		}
		
	};
	
	//Used for updating the miles reading
	private OnSeekBarChangeListener distListener_ = new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			distText_.setText(String.valueOf(distCalc(dist_.getProgress()))+" mi.");
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {	
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {

		}
		
	};
	
}
