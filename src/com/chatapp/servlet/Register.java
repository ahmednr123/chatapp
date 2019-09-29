package com.chatapp.servlet;

import java.io.IOException;

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
import java.util.logging.Logger;

/**
 *  - Route: /register
 *	- GET
 *		Redirct to /ChatApp/register.html
 *	- POST
 *		[username]
 *		[password]
 *		(onPass Redirect) /ChatApp/chat_app.html
 *		(onFail Redirect) /ChatApp/db_error.html
 */
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(Register.class.getName());

    public Register() {
        super();
    }

	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		response.sendRedirect("/ChatApp/register.html");
	}

	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		LOGGER.info("Registration request: \nUser: " + username + "\nPassword: " + password);
		
		boolean isUserRegistered = registerUser(username, password);
		
		if (!isUserRegistered) {
			response.sendRedirect("/ChatApp/db_error.html");
			return;
		}

		HttpSession session = request.getSession();
		session.setAttribute("username", username);
		response.sendRedirect("/ChatApp/chat_app.html");
	}

	/**
	 * To register a user
	 * 
	 * @param username User Id
	 * @param password User password
	 * @return isUserRegistered
	 */
	private synchronized
	boolean registerUser (String username, String password) {
		PreparedStatement stmt = null;
		Connection conn = null;

		boolean isUserRegistered = false;
		
		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
    		stmt.setString(1, username);
    		stmt.setString(2, password);
    		
    		stmt.executeUpdate();
    		isUserRegistered = true;
    	} catch (SQLException e) {
    		LOGGER.severe(e.getMessage());
    	} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
		}

		return isUserRegistered;
	}

}