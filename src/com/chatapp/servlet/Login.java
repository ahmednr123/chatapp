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

/**
 * Servlet implementation class Login
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
	
    public Login() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		response.sendRedirect("/LoginSession/login.html");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		System.out.println("Authentication Request!");
		
		System.out.println("Username: " + username);
		System.out.println("Password: " + password);
		
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
    		e.printStackTrace();
    	} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { rs.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
    	}
    	
    	return isUserValid;
	}

}