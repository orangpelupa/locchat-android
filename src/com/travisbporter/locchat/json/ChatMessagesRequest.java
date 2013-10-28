package com.travisbporter.locchat.json;

import com.google.gson.annotations.SerializedName;

public class ChatMessagesRequest {
	
	@SerializedName("Usr")
	public String usr;
	
	@SerializedName("Lat")
	public double lat;
	
	@SerializedName("Lon")
	public double lon;
	
	@SerializedName("Dist")
	public int dist;
}
