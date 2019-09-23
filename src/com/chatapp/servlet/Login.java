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
 *  - Route: /login
 *	- GET
 *		Redirect to /ChatApp/login.html
 *	- POST
 *		@param username 
 *		@param password
 *		(onPass Redirect) /ChatApp/chat_app.html
 *		(onFail Redirect) /ChatApp/auth_fail.html
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(Login.class.getName());

    public Login() {
        super();
    }

	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		response.sendRedirect("/ChatApp/login.html");
	}

	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		LOGGER.info("Authentication request: \nUser: " + username + "\nPassword: " + password);
		
		boolean isUserValid = validateUser(username, password);

		if (!isUserValid) {
			response.sendRedirect("/ChatApp/auth_fail.html");
			return;
		}

		HttpSession session = request.getSession();
		session.setAttribute("username", username);
		response.sendRedirect("/ChatApp/chat_app.html");
	}

	/**
	 * To validate a user
	 * 
	 * @param username
	 * @param password
	 * @return isUserValid
	 */
	private 
	boolean validateUser (String username, String password) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		
    	boolean isUserValid = false;
    	
    	try {
    		conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
        	stmt.setString(1, username);
        	stmt.setString(2, password);
    		
    		rs = stmt.executeQuery(); 
        	
        	if (rs.next()) {
        		isUserValid = true;
        	}
    	} catch (SQLException e) {
    		LOGGER.severe(e.getMessage());
    	} catch (Exception e) {
    		LOGGER.severe(e.getMessage());
    	} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { rs.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
    	}
    	
    	return isUserValid;
	}
}