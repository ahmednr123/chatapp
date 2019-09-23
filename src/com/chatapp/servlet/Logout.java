package com.chatapp.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.logging.Logger;

/**
 *  - Route: /login
 *	- GET
 *		Invalidate user session
 */
public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(Logout.class.getName());

    public Logout() {
        super();
    }

	protected 
	void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		try {
			HttpSession session = request.getSession(false);
			String username = (String) session.getAttribute("username");
			session.invalidate();

			LOGGER.info("User: " + username + " logged out!");

			response.sendRedirect("/ChatApp");
		} catch (NullPointerException e) {
			response.sendRedirect("/ChatApp");
		}
	}

	protected 
	void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
