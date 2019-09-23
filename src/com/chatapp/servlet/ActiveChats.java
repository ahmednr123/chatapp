package com.chatapp.servlet;

import java.util.ArrayList;
import java.util.logging.Logger;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;

import com.chatapp.util.DatabaseManager;

/**
 *  - Route: /active_chats
 *	- GET
 *		@param username (FROM SESSION)
 *		(json reply) Array of Active Chats Info
 *		(onFail reply) false
 */
public class ActiveChats extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(ActiveChats.class.getName());
	
    public ActiveChats() {
        super();
    }

	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(false);
		String username = null;

		try {
			username = (String)session.getAttribute("username");
		} catch (NullPointerException e) {
			LOGGER.info("User session not found!");
			out.println("false");
			out.close();
			return;
		}

		ArrayList<ChatInfo> activeChats = getActiveChats(username);
		out.println(ChatInfo.getJsonStringArray(activeChats));
		out.close();
	}

	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		doGet(request, response);
	}

	/**
	 * To retreive active chats of the user
	 * 
	 * @param username
	 * @return ArrayList<ChatInfo> 
	 *					Array of active chats retreived from the database 
	 */
	protected
	ArrayList<ChatInfo> getActiveChats (String username) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet res = null;

		ArrayList<ChatInfo> activeChats = new ArrayList<ChatInfo>();

		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM chat_manager WHERE user_one=? OR user_two=?");
			stmt.setString(1, username);
			stmt.setString(2, username);

			res = stmt.executeQuery();
			while (res.next()) {
				String receiver = null;

				// Pre set the receiver value to "user_one"
				String user_one = receiver = res.getString("user_one");
				String user_two = res.getString("user_two");
				
				// Change receiver value if "user_one" is the session user
				if (user_one.equals(username)) {
					receiver = user_two;
				}

				activeChats.add(new ChatInfo(res.getInt("chat_id"), receiver, res.getString("message_key")));
			}
		} catch (SQLException e) {
    		LOGGER.severe(e.getMessage());
    	} catch (Exception e) {
    		LOGGER.severe(e.getMessage());
		} finally {
			try { res.close(); } catch (Exception e) {}
			try { stmt.close(); } catch (Exception e) {}
			try { conn.close(); } catch (Exception e) {}
		}

		return activeChats;
	}
}

class ChatInfo {
	private int chat_id;
	private String username;
	private String message_key;

	public 
	ChatInfo (int chat_id, String username, String message_key) {
		if (username == null || message_key == null) {
			throw new NullPointerException();
		}

		this.chat_id = chat_id;
		this.username = username;
		this.message_key = message_key;
	}

	public String toJsonString () {
		return "{\"chat_id\":" + chat_id + 
				", \"username\":\"" + username + 
				"\", \"message_key\":\"" + message_key + "\"}";
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