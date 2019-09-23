package com.chatapp.servlet;

import java.util.Enumeration;
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
 *  - Route: /create_chat
 *	- POST
 *		@param receiver User to create chat with
 *		@param sender (FROM SESSION)
 *		(String reply) true or false
 */
public class CreateChat extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(CreateChat.class.getName());

	public CreateChat () {
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
		String receiver = request.getParameter("receiver");

		HttpSession session = request.getSession(false);

		try {
			sender = (String) session.getAttribute("username");
		} catch (NullPointerException e) {
			LOGGER.info("User session not found!");
			out.println("false");
			return;
		}

		// Check if appropriate parameters are passed with the request
		// and also check if the sender is not creating a chat with himself
		if (receiver == null || (sender == receiver)) {
			out.println("false");
			return;
		}

		LOGGER.info("Creating chat between \n[User]: " + sender + " and [User]: " + receiver);

		String message_key = randomKey(20);
		boolean isChatCreated = createChat(sender, receiver, message_key);

		if (!isChatCreated) {
			out.println("err");
			return;
		}

		out.println("true");
		out.close();
	}

	/**
	 * Create chat between two users
	 * 
	 * @param sender User sending message to the Chat
	 * @param receiver User to receive the message
	 * @param message_key Key used to encrypt and decrypt chat messagaes on client side
	 * @return isChatCreated
	 */
	protected
	boolean createChat (String sender, String receiver, String message_key) {
		System.out.println("Creating a chat");
		Connection conn = null;
		PreparedStatement stmt = null;

		boolean isChatCreated = false;
		boolean doesChatExist = doesChatExist(sender, receiver);

		if (doesChatExist) {
			System.out.println("[" + sender + ", " + receiver + "] pair already exists!");
			return false;
		}

		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("INSERT INTO chat_manager (user_one, user_two, message_key) VALUES (?,?,?)");
			stmt.setString(1, sender);
			stmt.setString(2, receiver);
			stmt.setString(3, message_key);
			
			stmt.executeUpdate();
			isChatCreated = true;
		} catch (SQLException e) {
    		LOGGER.severe(e.getMessage());
		} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
		}

		return isChatCreated;
	}

	/**
	 * Check if the chat already exists between the users
	 * 
	 * @param sender User sending message to the Chat
	 * @param receiver User to receive the message
	 * @return doesChatExist
	 */
	protected
	boolean doesChatExist (String sender, String receiver) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		boolean doesChatExist = false;

		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("SELECT chat_id FROM chat_manager WHERE (user_one=? OR user_two=?) AND (user_one=? OR user_two=?)");
			stmt.setString(1, sender);
			stmt.setString(2, sender);
			stmt.setString(3, receiver);
			stmt.setString(4, receiver);
			
			rs = stmt.executeQuery();
			if (rs.next()) {
				doesChatExist = true;
			}
		} catch (SQLException e) {
    		LOGGER.severe(e.getMessage());
		} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
		}

		return doesChatExist;
	}


	protected static 
	final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	/**
	 * Get random string
	 * 
	 * @param count Number of random characters to be added to the string
	 * @return Random String
	 */
	protected 
	static String randomKey(int count) {
		StringBuilder builder = new StringBuilder();

		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}

		return builder.toString();
	}
}