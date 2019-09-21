package com.chatapp.servlet;

import java.util.Enumeration;

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

public class SendMessage extends HttpServlet {

	public SendMessage () {
		super();
	}

	protected
	void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
			response.getWriter().println("false");
	}

	protected
	void doPost (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();
		String sender;
		String chat_id = request.getParameter("chat_id");
		String message = request.getParameter("message");
		
		if (chat_id == null || message == null) {
			out.println("{\"reply\":false}");
			return;
		}

		HttpSession session = request.getSession(false);

		try {
			sender = (String) session.getAttribute("username");
		} catch (NullPointerException e) {
			System.out.println("session not found!");
			out.println("{\"reply\":false}");
			out.close();
			return;
		}

		Timestamp datetime = sendMessage(chat_id, username, message);

		if (datetime == null) {
			out.println("{\"reply\":false}");
			out.close();
			return;
		}

		out.println("{\"reply\":true,\"message\": \"" + message 
						+ "\", \"time\": \"" + datetime.toString() + "\"}");
		out.close();
	}

	protected Timestamp sendMessage (int chat_id, String username, String message) {
		System.out.println("Sending a message");
		Connection conn = null;
		PreparedStatement stmt = null;
		Timestamp datetime = null;

		try {
			conn = DatabaseManager.getConnection();

			stmt = conn.prepareStatement("SELECT NOW() as time");
			rs = stmt.executeQuery(); 
			rs.next();
			
			datetime = rs.getTimestamp("time");

			stmt = conn.prepareStatement("INSERT INTO chats (chat_id, sender, message, time) VALUES (?,?,?,?)");
			stmt.setString(1, chat_id);
			stmt.setString(2, username);
			stmt.setString(3, message);
			stmt.setTimestamp(4, datetime);
			
			stmt.executeUpdate();
		} catch (SQLException e) {
    		e.printStackTrace();
		} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
		}

		return datetime;
	}
}