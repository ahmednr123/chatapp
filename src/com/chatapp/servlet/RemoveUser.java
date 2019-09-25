package com.chatapp.servlet;

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
import java.util.logging.Logger;

public class RemoveUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(AddUser.class.getName());

    public RemoveUser () {
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

        if ( !isUserAuthorized(session_user, chat_id) ) {
            LOGGER.info("User: " + session_user + " accessed an unauthorized group");
            out.print("false");
            out.close();
            return;
        }

        boolean isUserRemoved = removeUser(chat_id, username);

        if (!isUserRemoved) {
            out.print("err");
            return;
        }

        out.print("true");
        out.close();
    }

    /**
     * Remove a user from the group
     *
     * @param chat_id
     * @param username
     * @return
     */
    private
    boolean removeUser (int chat_id, String username) {
        boolean isUserRemoved = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.prepareStatement("DELETE FROM chat_users WHERE chat_id=? AND username=?");
            stmt.setInt(1, chat_id);
            stmt.setString(2, username);
            stmt.executeUpdate();

            stmt = conn.prepareStatement("DELETE FROM chat_manager WHERE id NOT IN (SELECT chat_id FROM chat_users)");
            stmt.executeUpdate();

            isUserRemoved = true;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }

        return isUserRemoved;
    }

    /**
     * Check is the user is authorized to remove users to the group
     *
     * @param username
     * @param chat_id
     * @return
     */
    private
    boolean isUserAuthorized (String username, int chat_id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean isUserAuthorized = false;

        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.prepareStatement("SELECT username FROM chat_users WHERE chat_id IN (SELECT chat_id FROM chat_groups WHERE chat_id=?) AND username=?");
            stmt.setInt(1, chat_id);
            stmt.setString(2, username);

            rs = stmt.executeQuery();
            if (rs.next()) {
                isUserAuthorized = true;
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        } finally {
            try { rs.close(); } catch (Exception e) {}
            try { stmt.close(); } catch (Exception e) {}
            try { conn.close(); } catch (Exception e) {}
        }

        return isUserAuthorized;
    }
}