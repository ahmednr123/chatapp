package com.chatapp.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class ElasticManager {
    private static Logger LOGGER = Logger.getLogger(ElasticManager.class.getName());
    private static String URL = "http://localhost:9200";

    private ElasticManager () {}

    // HANDLE BETTER
    public
    static void setBaseUrl (String location, int port, String protocol) {
        ElasticManager.URL = protocol + "://" + location + ":" + port;
    }

    public
    static JSONObject get (String relative_path)
            throws IOException, JSONException
    {
        return HttpRequest("GET", URL + relative_path);
    }

    public
    static JSONObject delete (String relative_path)
            throws IOException, JSONException
    {
        return HttpRequest("DELETE", URL + relative_path);
    }

    public
    static JSONObject get (String relative_path, String JSON)
            throws IOException, JSONException
    {
        return HttpRequest("GET", URL + relative_path, JSON);
    }

    public
    static JSONObject post (String relative_path, String JSON)
            throws IOException, JSONException
    {
        return HttpRequest("POST", URL + relative_path, JSON);
    }

    public
    static JSONObject put (String relative_path, String JSON)
            throws IOException, JSONException
    {
        return HttpRequest("PUT", URL + relative_path, JSON);
    }

    public
    static JSONObject delete (String relative_path, String JSON)
            throws IOException, JSONException
    {
        return HttpRequest("DELETE", URL + relative_path, JSON);
    }

    private
    static JSONObject HttpRequest (String method, String URL)
            throws IOException, JSONException
    {
        return HttpRequest(method, URL, null);
    }

    private
    static JSONObject HttpRequest (String method, String URL, String JSON)
            throws IOException, JSONException
    {
        String str, jsonString = "";
        JSONObject responseObj;

        HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        if (JSON != null) {
            connection.setDoOutput(true);

            OutputStream request = connection.getOutputStream();
            request.write(JSON.getBytes("UTF-8"));
            request.close();
        }

        InputStream response;

        try {
            response = connection.getInputStream();
        } catch (Exception e) {
            response = connection.getErrorStream();
        }

        BufferedReader bf = new BufferedReader(new InputStreamReader(response));
        while ((str = bf.readLine()) != null){
            jsonString += str;
        };
        bf.close();

        responseObj = new JSONObject(jsonString);
        responseObj.put("status", connection.getResponseCode());

        return responseObj;
    }
}