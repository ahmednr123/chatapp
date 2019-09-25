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
import java.util.ArrayList;
import java.util.logging.Logger;

public class GetUsers extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(GetUsers.class.getName());

    public GetUsers() {
        super();
    }

    protected
    void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        String session_user = null;
        int chat_id;

        try {
            session_user = (String) session.getAttribute("username");
            chat_id = Integer.parseInt(request.getParameter("chat_id"));
        } catch (NullPointerException e) {
            LOGGER.severe(e.getMessage());
            out.print("false");
            out.close();
            return;
        }

        if ( !isUserAuthorized(session_user, chat_id) ) {
            LOGGER.info("User: " + session_user + " accessed an unauthorized group");
            out.print("false");
            out.close();
            return;
        }

        ArrayList<String> groupUsers = getUsers(chat_id);

        out.print(jsonStringArray(groupUsers));
        out.close();
    }

    protected
    void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet(request, response);
    }

    /**
     * Get users that are members of the group
     *
     * @param chat_id
     * @return
     */
    private
    ArrayList<String> getUsers (int chat_id) {
        ArrayList<String> groupUsers = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            stmt = conn.prepareStatement("SELECT username FROM chat_users WHERE chat_id=?");
            stmt.setInt(1, chat_id);

            rs = stmt.executeQuery();
            while (rs.next()) {
                groupUsers.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }

        return groupUsers;
    }

    /**
     * Check is the user is authorized to get users data from the group
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

    private
    String jsonStringArray (ArrayList<String> array) {
        String jsonArray = "[";

        if (array.size() > 0) {
            for (String element : array) {
                jsonArray += "\"" + element + "\",";
            }

            jsonArray = jsonArray.substring(0, jsonArray.length() - 1);
        }

        jsonArray = jsonArray + "]";

        return jsonArray;
    }

}
