package com.travisbporter.locchat.json;

import java.util.List;

import com.google.gson.annotations.*;

public class ChatMessages {
	@SerializedName("Msgs")
	public List<ChatMessage> msgs;
}
