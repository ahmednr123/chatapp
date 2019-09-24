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
import java.sql.Timestamp;

import com.chatapp.util.DatabaseManager;

/**
 *  - Route: /messages
 *	- GET
 *		@param type Type of messages to get from database
 *		@param chat_id Chat ID corresponding to the users chat
 *		@param msg_id [OPTIONAL] Message ID use dependent on the type of messages required
 *		@param username (FROM SESSION)
 *		(json reply) Array of Chat Messages
 *		(onFail reply) false
 */
public class GetMessages extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(GetMessages.class.getName());

	private static final int MESSAGE_TYPE_NEW = 0;
	private static final int MESSAGE_TYPE_OLD = 1;
	private static final int MESSAGE_TYPE_CURRENT = 2;
	
    public GetMessages() {
        super();
    }

	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(false);

		int chat_id, type, msg_id;

		// Check if appropriate parameters are passed with the request
		// Also check if a user session exists
		try {
			chat_id = Integer.parseInt(request.getParameter("chat_id"));
			type = Integer.parseInt(request.getParameter("type"));
			session.getAttribute("username");
		} catch (NullPointerException e) {
			out.println("false");
			out.close();
			return;
		}

		ArrayList<ChatMessage> chatMessages = getChatMessages(type, chat_id);

		// msg_id (Optional Parameter)
		// Check if msg_id is passed and accordingly execute
		// the getChatMessages method
		if (request.getParameter("msg_id") == null) {
			chatMessages = getChatMessages(type, chat_id);
		} else {
			msg_id = Integer.parseInt(request.getParameter("msg_id"));
			chatMessages = getChatMessages(type, chat_id, msg_id);
		}

		LOGGER.info("Get Messages request! \nchat_id: " + chat_id + " \ntype: " + type);

		out.println(ChatMessage.getJsonStringArray(chatMessages));
		out.close();
	}

	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		doGet(request, response);
	}

	/**
	 * To retreive chat messages from the database
	 * 
	 * @param type Type of the messages needed to fetch from the database
	 * @param chat_id Unique Chat ID corresponding to individual user chats
	 * @return ArrayList<ChatMessages> 
	 *					Array of chat messages retreived from the database 
	 */
	protected
	ArrayList<ChatMessage> getChatMessages (int type, int chat_id) {
		return getChatMessages (type, chat_id, -1);
	}

	/**
	 * To retreive chat messages from the database
	 *
	 * Three types of messages can be retreived from the database:
	 *		MESSAGE_TYPE_NEW
	 *			retrieves atmost 10 messages from the database
	 *			(messages after the given msg_id)
	 *		MESSAGE_TYPE_OLD
	 *			retrieves atmost 10 messages from the database
	 *			(messages before the given msg_id)
	 *		MESSAGE_TYPE_CURRENT
	 *			retrieves atmost 10 latest messages from the database
	 * 
	 * @param type Type of the messages needed to fetch from the database
	 * @param chat_id Unique Chat ID corresponding to individual user chats
	 * @param msg_id Message ID used as per the type of messages to be retreived
	 * @return ArrayList<ChatMessages> 
	 *					Array of chat messages retreived from the database 
	 */
	protected
	ArrayList<ChatMessage> getChatMessages (int type, int chat_id, int msg_id) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet res = null;

		ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

		try {
			conn = DatabaseManager.getConnection();

			switch (type) {
				case MESSAGE_TYPE_NEW:
					System.out.println("GETTING NEW");
					stmt = conn.prepareStatement("SELECT * FROM chats WHERE chat_id=? AND msg_id>? ORDER BY msg_id ASC LIMIT 10");
					stmt.setInt(1, chat_id);
					stmt.setInt(2, msg_id);
					break;
				case MESSAGE_TYPE_OLD:
					System.out.println("GETTING OLD");
					stmt = conn.prepareStatement("SELECT * FROM chats WHERE chat_id=? AND msg_id<? ORDER BY msg_id DESC LIMIT 10");
					stmt.setInt(1, chat_id);
					stmt.setInt(2, msg_id);
					break;
				case MESSAGE_TYPE_CURRENT:
					System.out.println("GETTING CURRENT");
					stmt = conn.prepareStatement("SELECT * FROM chats WHERE chat_id=? ORDER BY msg_id DESC LIMIT 10");
					stmt.setInt(1, chat_id);
					break;
			}

			res = stmt.executeQuery();
			while (res.next()) {
				chatMessages.add(
					new ChatMessage(res.getInt("msg_id"),
						res.getString("sender"), res.getString("message"), 
						res.getTimestamp("time"))
				);
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

		return chatMessages;
	}
}

class ChatMessage {
	private int msg_id;
	private String sender;
	private String message;
	private String time;

	public ChatMessage (int msg_id, String sender, String message, Timestamp time) {
		if (sender == null || message == null || time == null) {
			throw new NullPointerException();
		}

		this.msg_id = msg_id;
		this.sender = sender;
		this.message = message;
		this.time = time.toString();
	}

	public String toJsonString () {
		return "{\"msg_id\": " + msg_id
				+ ",\"sender\":\"" + sender 
				+ "\", \"message\": \"" + message 
				+ "\", \"time\": \"" + time + "\"}";
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