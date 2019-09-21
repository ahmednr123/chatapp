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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();

		// Maybe wrap these around try catch block while getting
		// value instead of so many if statements
		String chat_id_string = request.getParameter("chat_id");
		String type_string = request.getParameter("type");
		String msg_id_string = request.getParameter("msg_id");

		HttpSession session = request.getSession(false);
		String username = null;

		if (type_string == null || chat_id_string == null) {
			System.out.println("Some or All Parameters missing");
			out.println("false");
			out.close();
			return;
		}

		int chat_id = Integer.parseInt(chat_id_string);
		int type = Integer.parseInt(type_string);
		int msg_id = -1;

		if (msg_id_string != null)
			msg_id = Integer.parseInt(msg_id_string);

		try {
			username = (String)session.getAttribute("username");
		} catch (NullPointerException e) {
			System.out.println("No Session");
			out.println("false");
			out.close();
			return;
		}

		ArrayList<ChatMessage> chatMessages = getChatMessages(type, chat_id, msg_id);
		out.println(ChatMessage.getJsonStringArray(chatMessages));
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		doGet(request, response);
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
					stmt = conn.prepareStatement("SELECT * FROM chats WHERE chat_id=? AND msg_id>? ORDER BY msg_id ASC LIMIT 20");
					stmt.setInt(1, chat_id);
					stmt.setInt(2, msg_id);
					break;
				case MESSAGE_TYPE_OLD:
					System.out.println("GETTING OLD");
					stmt = conn.prepareStatement("SELECT * FROM chats WHERE chat_id=? AND msg_id<? ORDER BY msg_id DESC LIMIT 20");
					stmt.setInt(1, chat_id);
					stmt.setInt(2, msg_id);
					break;
				case MESSAGE_TYPE_CURRENT:
					System.out.println("GETTING CURRENT");
					stmt = conn.prepareStatement("SELECT * FROM chats WHERE chat_id=? ORDER BY msg_id DESC LIMIT 20");
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