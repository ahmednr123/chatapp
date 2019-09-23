package com.chatapp.servlet;

import java.util.logging.Logger;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *  - Route: /session
 *	- GET 
 *		(String reply) true or false
 *		Depicting if the user session was found or not
 */
public class Session extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(Session.class.getName());
	
    public Session() {
        super();
    }

	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(false);

		LOGGER.info("Session check requested");

		try {
			String username = (String)session.getAttribute("username");
			LOGGER.info("Session: " + username);
			out.println(username);
		} catch (NullPointerException e) {
			LOGGER.info("No session found");
			out.println("false");
		}
	}

	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		doGet(request, response);
	}
}
