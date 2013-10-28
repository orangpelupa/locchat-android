package com.travisbporter.locchat.linx.json;

import com.google.gson.annotations.SerializedName;

public class Response {
	@SerializedName("url")
	public String url;
	
	@SerializedName("filename")
	public String filename;
	
	@SerializedName("raw_url")
	public String urlRaw;
	
	@SerializedName("expiry")
	public long expiry;
	
	@SerializedName("delete_key")
	public String deleteKey;
	
	@SerializedName("size")
	public int size;
	
	@SerializedName("short_link")
	public String urlShort;
	
	@SerializedName("md5sum")
	public String md5sum;
	
}
