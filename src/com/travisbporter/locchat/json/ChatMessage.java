package com.travisbporter.locchat.json;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
	@SerializedName("Msg")
	public String msg;
	
	@SerializedName("Usr")
	public String usr;
	
	@SerializedName("Time")
	public long time;
	
	@SerializedName("Lat")
	public double lat;
	
	@SerializedName("Lon")
	public double lon;
}
