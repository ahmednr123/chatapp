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

import java.util.ArrayList;
import com.chatapp.util.DatabaseManager;

/**
 * Servlet implementation class Dashboard
 */
public class ActiveChats extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public ActiveChats() {
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

		HttpSession session = request.getSession(false);
		String username = null;
		int chat_id = request.getParameter("chat_id");

		try {
			username = (String)session.getAttribute("username");
		} catch (NullPointerException e) {
			System.out.println("No Session");
			out.println("false");
			out.close();
			return;
		}

		ArrayList<ChatMessage> chatMessages = getActiveChats(chat_id);
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
	ArrayList<ChatMessage> getChatMessages (int chat_id) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet res = null;

		ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM chats WHERE chat_id=? ORDER BY time DESC");
			stmt.setString(1, chat_id);

			res = stmt.executeQuery();
			while (res.next()) {
				chatMessages.add(
					new ChatMessage(res.getInt("chat_id"), 
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
	private int chat_id;
	private String sender;
	private String message;
	private String time;

	public ChatMessage (int chat_id, String sender, String, message, Timestamp time) {
		if (sender == null || message == null || time == null) {
			throw NullPointerException();
		}

		this.chat_id = chat_id;
		this.sender = sender;
		this.message = message;
		this.time = time.toString();
	}

	public String toJsonString () {
		return "{\"chat_id\":" + chat_id + 
				",\"sender\":\"" + username + 
				",\"message\": \"" + message + 
				",\"time\": \"" + time + "\"}";
	}

	public static String toJsonStringArray (ArrayList<ChatMessage> chatMessages) {
		String arrayString = "[";

		for (ChatMessage chatMessage : chatMessages) {
			arrayString += chatMessage.toJsonString() + ",";
		}

		arrayString = arrayString.substring(0, arrayString.length() - 1);
		return arrayString + "]";
	}
}