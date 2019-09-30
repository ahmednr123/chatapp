package com.chatapp.format;

import java.util.ArrayList;

public class ChatMessage {
    private String msg_id;
    private String sender;
    private String message;
    private String timestamp;

    public ChatMessage (String msg_id, String sender, String message, String timestamp) {
        if (msg_id == null || sender == null || message == null || timestamp == null) {
            throw new NullPointerException();
        }

        this.msg_id = msg_id;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String toJsonString () {
        return "{\"msg_id\": \"" + msg_id
                + "\", \"sender\":\"" + sender
                + "\", \"message\": \"" + message
                + "\", \"timestamp\": \"" + timestamp + "\"}";
    }

    public static String getJsonStringArray (ArrayList<ChatMessage> chatMessages) {
        String arrayString = "[";

        if (chatMessages.size() > 0) {
            for (ChatMessage chatMessage : chatMessages) {
                arrayString += chatMessage.toJsonString() + ",";
            }

            arrayString = arrayString.substring(0, arrayString.length() - 1);
        }

        return arrayString + "]";
    }
}
