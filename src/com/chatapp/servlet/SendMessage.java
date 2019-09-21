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
			out.println("false");
			return;
		}

		HttpSession session = request.getSession(false);

		try {
			sender = (String) session.getAttribute("username");
		} catch (NullPointerException e) {
			System.out.println("session not found!");
			out.println("false");
			out.close();
			return;
		}

		boolean isMessageSent = sendMessage(chat_id, username, message);

		if (!isMessageSent) {
			out.println("false");
			out.close();
			return;
		}

		out.println("true");
		out.close();
	}

	protected boolean sendMessage (int chat_id, String username, String message) {
		System.out.println("Sending a message");
		Connection conn = null;
		PreparedStatement stmt = null;

		boolean isMessageSent = false;

		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("INSERT INTO chats (chat_id, sender, message, time) VALUES (?,?,?,NOW())");
			stmt.setString(1, chat_id);
			stmt.setString(2, username);
			stmt.setString(3, message);
			
			stmt.executeUpdate();
			isMessageSent = true;
		} catch (SQLException e) {
    		e.printStackTrace();
		} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
		}

		return isMessageSent;
	}
}