package com.chatapp.format;

import java.util.ArrayList;

public class ChatInfo {
    private int chat_id;
    private String username;
    private String message_key;
    private String group_name;

    public
    ChatInfo (int chat_id, String username, String message_key, String group_name) {
        if (username == null || message_key == null) {
            throw new NullPointerException();
        }

        this.chat_id = chat_id;
        this.username = username;
        this.message_key = message_key;
        this.group_name = group_name;
    }

    public
    ChatInfo (int chat_id, String username, String message_key) {
        this(chat_id, username, message_key, "");
    }

    public String toJsonString () {
        return "{\"chat_id\":" + chat_id +
                ", \"username\":\"" + username +
                "\", \"message_key\":\"" + message_key +
                "\", \"group_name\": \"" + group_name + "\"}";
    }

    public static String getJsonStringArray (ArrayList<ChatInfo> chats) {
        String arrayString = "[";

        if (chats.size() > 0) {
            for (ChatInfo chat : chats) {
                System.out.println(chat.toJsonString());
                arrayString += chat.toJsonString() + ",";
            }

            arrayString = arrayString.substring(0, arrayString.length() - 1);
        }

        return arrayString + "]";
    }
}
