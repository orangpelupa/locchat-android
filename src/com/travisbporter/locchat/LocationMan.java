
package com.travisbporter.locchat;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;


//wraps up location listening, use LocationChangeListener interface to get location changes

public class LocationMan implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener  {
	
    public static final String TAG = "LocationMan";
    public static final int REQ_INTERVAL = 10000;
    public static final int MIN_UPDATE_TIME = 10000;
    public static final int MIN_UPDATE_DIST = 1000;
    
    
    private LocationChangeListener listener_;
    public interface LocationChangeListener {
        public void onLocationChange(Location location);
    }

    private Context context_;
    private LocationRequest locReq_;
    private LocationClient locClient_;
    
    public void start(){
        Log.d(TAG, "start()");
        
        locClient_ = new LocationClient(context_, this, this);
        locClient_.connect();
    }
    
    public void stop() {
        locClient_.removeLocationUpdates(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        
        locReq_ = new LocationRequest();
        
        locReq_.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locReq_.setSmallestDisplacement(1);
        locReq_.setNumUpdates(1);
        locReq_.setInterval(REQ_INTERVAL);
        
        locClient_.requestLocationUpdates(locReq_, this);
        listener_.onLocationChange(locClient_.getLastLocation());
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }


    private static LocationMan instance;

    public static synchronized LocationMan getInstance(Context context, LocationChangeListener listener){
        if (instance == null) {
            instance = new LocationMan(context, listener);
        }
        return instance;
    }


    private LocationMan(Context c, LocationChangeListener l){
        context_ = c;
        listener_ = l;
    }


    @Override
    public void onLocationChanged(Location l) {
        Log.d(TAG, "onLocationChanged");
        
        listener_.onLocationChange(l);
    }

}