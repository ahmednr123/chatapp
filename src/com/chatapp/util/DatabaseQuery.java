package com.chatapp.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseQuery {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(DatabaseQuery.class.getName());

    /**
     * Blocking Object Creation
     */
    private DatabaseQuery () {
    }

    /**
     * Check is the user is authorized to remove users to the group
     *
     * @param username
     * @param chat_id
     * @return
     */
    public static
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
