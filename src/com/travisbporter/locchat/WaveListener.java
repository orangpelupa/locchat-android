package com.travisbporter.locchat;

import com.google.android.gms.location.LocationClient;
import com.travisbporter.locchat.LocationMan.LocationChangeListener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

//Listener that just waits for a "wave" motion on the accelerometer
//Can be statically stopped/started
public class WaveListener implements SensorEventListener {
	
    private WaveDetectListener listener_;
    public interface WaveDetectListener {
        public void onWaveDetected();
    }
    
    private static SensorManager sensorMan_;

    protected Context context_;
    private static WaveListener instance_;
    private static boolean started_;
    
    public static synchronized WaveListener getInstance(Context c, WaveDetectListener l){
        if (instance_ == null) {
        	instance_ = new WaveListener(c, l);
        }
        return instance_;
    }
    
    public static synchronized WaveListener getInstance(){
    	return instance_;
    }
    
    private WaveListener(Context c,WaveDetectListener l){
    	context_ = c;
    	listener_ = l;
    	
    }
    public static synchronized void start(){
    	if(!started_ && getInstance()!=null){
    		sensorMan_ = (SensorManager) getInstance().context_.getSystemService(getInstance().context_.SENSOR_SERVICE);
    		sensorMan_.registerListener(getInstance(), sensorMan_.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    		started_ = true;
    	}
    }
    
    public static synchronized void stop(){
    	if(getInstance()!=null){
    		sensorMan_.unregisterListener(getInstance());
    		started_ = false;
    	}
    }
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		//Umm these kinda work for detecting a wave motion!
		if(x>15 || y>15 || z>15){
			listener_.onWaveDetected();
		}
	}

}
