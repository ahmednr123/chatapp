package com.chatapp.servlet;

import com.chatapp.format.ChatInfo;
import com.chatapp.util.DatabaseManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *  - Route: /active_chats
 *	- GET
 *		[username] (FROM SESSION)
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
		ArrayList<ChatInfo> activeGroups = getActiveGroups(username);
		activeChats.addAll(activeGroups);

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
			stmt = conn.prepareStatement("SELECT chat_users.chat_id, chat_users.username, chat_manager.message_key from chat_users INNER JOIN chat_manager ON chat_manager.id = chat_users.chat_id WHERE chat_id IN (SELECT chat_id from chat_users WHERE chat_id NOT IN (SELECT chat_id FROM chat_groups) AND username=?) AND username!=?");
			stmt.setString(1, username);
			stmt.setString(2, username);

			res = stmt.executeQuery();
			while (res.next()) {
				activeChats.add(new ChatInfo(res.getInt("chat_id"), res.getString("username"), res.getString("message_key")));
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
	/**
	 * To retreive active chats of the user
	 *
	 * @param username
	 * @return ArrayList<ChatInfo>
	 *					Array of active chats retreived from the database
	 */
	protected
	ArrayList<ChatInfo> getActiveGroups (String username) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet res = null;

		ArrayList<ChatInfo> activeGroups = new ArrayList<ChatInfo>();

		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("SELECT chat_groups.chat_id, chat_groups.name, chat_manager.message_key from chat_groups INNER JOIN chat_manager ON chat_manager.id = chat_groups.chat_id INNER JOIN chat_users ON chat_users.chat_id = chat_groups.chat_id WHERE chat_users.username=?");
			stmt.setString(1, username);

			res = stmt.executeQuery();
			while (res.next()) {
				activeGroups.add(
						new ChatInfo(
							res.getInt("chat_id"),
							"",
							res.getString("message_key"),
							res.getString("name")
						)
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

		return activeGroups;
	}

}