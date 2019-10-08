package com.chatapp.servlet.groups;

import com.chatapp.util.DatabaseManager;
import com.chatapp.util.DatabaseQuery;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 *  - Route: /add_user
 *	- POST
 *      [chat_id] Group chat id
 *		[username] User to be added
 *		[session_user] (FROM SESSION) to verify session_users privilege
 *		(String reply) true or false
 */
@WebServlet(urlPatterns = "/add_user")
public class AddUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(AddUser.class.getName());

    public AddUser() {
        super();
    }

    protected
    void doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.getWriter().print("404 - error");
    }

    protected
    void doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        String username = request.getParameter("username");

        String session_user;
        int chat_id;

        try {
            chat_id = Integer.parseInt(request.getParameter("chat_id"));
            session_user = (String) session.getAttribute("username");
        } catch (NullPointerException e) {
            LOGGER.severe(e.getMessage());
            out.print("false");
            out.close();
            return;
        }

        if (username == null) {
            out.print("false");
            return;
        }

        if ( !DatabaseQuery.isUserAuthorized(session_user, chat_id) ) {
            LOGGER.info("User: " + session_user + " accessed an unauthorized group");
            out.print("false");
            out.close();
            return;
        }

        boolean isUserAdded = addUser(chat_id, username);

        if (!isUserAdded) {
            out.print("err");
            return;
        }

        out.print("true");
        out.close();
    }

    /**
     * Add user to the group
     *
     * @param chat_id
     * @param username
     * @return
     */
    private synchronized
    boolean addUser (int chat_id, String username) {
        boolean isUserAdded = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.prepareStatement("INSERT INTO chat_users (chat_id, username) VALUES (?,?)");
            stmt.setInt(1, chat_id);
            stmt.setString(2, username);

            stmt.executeUpdate();
            isUserAdded = true;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }

        return isUserAdded;
    }
}