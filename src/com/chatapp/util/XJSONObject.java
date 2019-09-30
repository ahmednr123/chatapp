package com.chatapp.util;

import org.json.JSONArray;
import org.json.JSONObject;

enum State {
    AC1, AC3, SHORT_STRING, OPEN_BRACKET, CLOSE_BRACKET, END, DOT, ERROR
}
public class XJSONObject {
    private boolean createOnFly;
    JSONObject jsonObject;

    public XJSONObject () {
        jsonObject = new JSONObject();
        createOnFly = false;
    }

    public XJSONObject (String json) {
        jsonObject = new JSONObject(json);
        createOnFly = false;
    }

    public XJSONObject (JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        createOnFly = false;
    }

    /* Create objects as parsing them while using put() method
    public void setCreateOnFly(boolean createOnFly) {
        this.createOnFly = createOnFly;
    }*/

    Object parseObject (String query) {
        Object tempObj = jsonObject;

        int index = 0;
        State state = State.AC1;
        String buffer = "";

        while (index < query.length()) {
            char ch = query.charAt(index);
            switch (state) {
                case AC1:
                    buffer = "";
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else {
                        System.out.println("CANNOT START WITH NUMBER");
                        state = State.ERROR;
                    }
                    break;
                case SHORT_STRING:
                    if (ch == '.') {
                        state = State.DOT;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                    } else if (index + 1 == query.length()) {
                        state = State.END;
                    }

                    if (state == State.OPEN_BRACKET || state == State.DOT) {
                        // BUILD OBJ
                        System.out.println("BUILDING OBJECT: " + buffer);
                        tempObj = ((JSONObject) tempObj).get(buffer);
                        buffer = "";
                    } else if (state == State.END) {
                        buffer += ch;
                        tempObj = ((JSONObject) tempObj).get(buffer);
                        buffer = "";
                    } else {
                        buffer += ch;
                    }
                    index++;
                    break;
                case OPEN_BRACKET:
                    if (ch == ']') {
                        state = State.CLOSE_BRACKET;
                    } else {
                        buffer += ch;
                        index++;
                    }
                    break;
                case CLOSE_BRACKET:
                    System.out.println("Received buffer: " + buffer);
                    try {
                        int num = Integer.parseInt(buffer);
                        tempObj = ((JSONArray) tempObj).get(num);
                    } catch (NumberFormatException e) {
                        tempObj = ((JSONObject) tempObj).get(buffer);
                    }
                    buffer = "";
                    state = State.AC3;
                    index++;
                    break;
                case AC3:
                    if (ch == '.') {
                        state = State.DOT;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else if (index+1 == query.length()) {
                        state = State.END;
                    } else {
                        System.out.println("FROM AC3");
                        state = State.ERROR;
                    }
                    break;
                case DOT:
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else {
                        System.out.println("FROM DOT");
                        state = State.ERROR;
                    }
                    break;
            }
            if (state == State.ERROR) {
                System.out.println("WRONG QUERY");
                throw new RuntimeException();
            }
        }

        return tempObj;
    }

    public Object get (String query) throws RuntimeException {
        return parseObject(query);
    }

    public void put (String query, Object obj) {
        String key = null;
        Object tempObj = jsonObject;

        int index = 0;
        State state = State.AC1;
        String buffer = "";

        while (index < query.length()) {
            char ch = query.charAt(index);
            switch (state) {
                case AC1:
                    buffer = "";
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else {
                        System.out.println("CANNOT START WITH NUMBER");
                        state = State.ERROR;
                    }
                    break;
                case SHORT_STRING:
                    if (ch == '.') {
                        state = State.DOT;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                    } else if (index + 1 == query.length()) {
                        state = State.END;
                    }

                    if (state == State.OPEN_BRACKET || state == State.DOT) {
                        // BUILD OBJ
                        tempObj = ((JSONObject) tempObj).get(buffer);
                        buffer = "";
                    } else if (state == State.END) {
                        buffer += ch;
                        key = buffer;
                        buffer = "";
                    } else {
                        buffer += ch;
                    }
                    index++;
                    break;
                case OPEN_BRACKET:
                    if (ch == ']') {
                        state = State.CLOSE_BRACKET;
                    } else {
                        buffer += ch;
                        index++;
                    }
                    break;
                case CLOSE_BRACKET:
                    if (index + 1 == query.length()) {
                        key = buffer;
                        index++;
                        break;
                    }
                    try {
                        int num = Integer.parseInt(buffer);
                        tempObj = ((JSONArray) tempObj).get(num);
                    } catch (NumberFormatException e) {
                        tempObj = ((JSONObject) tempObj).get(buffer);
                    }
                    buffer = "";
                    state = State.AC3;
                    index++;
                    break;
                case AC3:
                    if (ch == '.') {
                        state = State.DOT;
                        index++;
                    } else if (ch == '[') {
                        state = State.OPEN_BRACKET;
                        index++;
                    } else {
                        System.out.println("FROM AC3");
                        state = State.ERROR;
                    }
                    break;
                case DOT:
                    if (Character.isAlphabetic(ch) || ch == '_') {
                        state = State.SHORT_STRING;
                    } else {
                        System.out.println("FROM DOT");
                        state = State.ERROR;
                    }
                    break;
            }
            if (state == State.ERROR) {
                System.out.println("WRONG QUERY");
                throw new RuntimeException();
            }
        }

        if (tempObj instanceof JSONArray) {
            int intKey = Integer.parseInt(key);
            ((JSONArray)tempObj).put(intKey, obj);
        } else if (tempObj instanceof JSONObject) {
            ((JSONObject)tempObj).put(key, obj);
        }
    }

    public String toString () {
        return jsonObject.toString();
    }
}