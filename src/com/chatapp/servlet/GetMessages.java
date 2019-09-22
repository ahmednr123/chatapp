package com.chatapp.servlet;

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

import java.util.ArrayList;
import com.chatapp.util.DatabaseManager;

/**
 * Servlet implementation class Dashboard
 */
public class GetMessages extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
		String username = null;

		int chat_id, type, msg_id;

		try {
			chat_id = Integer.parseInt(request.getParameter("chat_id"));
			type = Integer.parseInt(request.getParameter("type"));
			username = (String)session.getAttribute("username");
		} catch (NullPointerException e) {
			out.println("false");
			out.close();
			return;
		}

		ArrayList<ChatMessage> chatMessages;
		String msg_id_string = request.getParameter("msg_id");

		if (msg_id_string == null) {
			chatMessages = getChatMessages(type, chat_id);
		} else {
			msg_id = Integer.parseInt(msg_id_string);
			chatMessages = getChatMessages(type, chat_id, msg_id);
		}

		out.println(ChatMessage.getJsonStringArray(chatMessages));
		out.close();
	}

	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		doGet(request, response);
	}

	protected
	ArrayList<ChatMessage> getChatMessages (int type, int chat_id) {
		return getChatMessages (type, chat_id, -1);
	}

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
			e.printStackTrace();
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