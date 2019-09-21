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
 * Servlet implementation class Signup
 */
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		response.sendRedirect("/ChatApp/register.html");
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
		
		System.out.println("Registration Request!");
		
		System.out.println("Username: " + username);
		System.out.println("Password: " + password);
		
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
	 * @param username
	 * @param password
	 * @return isUserRegistered
	 */
	private
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
    		e.printStackTrace();
    	} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
		}

		return isUserRegistered;
	}

}