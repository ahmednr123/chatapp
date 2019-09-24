package com.chatapp.servlet;

import java.util.Enumeration;
import java.util.logging.Logger;

import java.io.IOException;
import java.io.PrintWriter;

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
 *  - Route: /create_chat
 *	- POST
 *		@param receiver User to create chat with
 *		@param sender (FROM SESSION)
 *		(String reply) true or false
 */
public class CreateChat extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(CreateChat.class.getName());

    public static final int USER_CHAT = 0;
    public static final int GROUP_CHAT = 1;

    public CreateChat () {
        super();
    }

    protected
    void doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.getWriter().println("false");
    }

    protected
    void doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        String sender, type;

        HttpSession session = request.getSession(false);

        try {
            type = Integer.parseInt(request.getParameter("type"));
            sender = (String) session.getAttribute("username");
        } catch (NullPointerException e) {
            LOGGER.info("User session not found!");
            out.println("false");
            return;
        }

        String message_key = randomKey(20);
        boolean isChatCreated = false;
        switch (type) {
            USER_CHAT:
            String receiver = request.getParameter("receiver");

            // Check if appropriate parameters are passed with the request
            // and also check if the sender is not creating a chat with himself
            if (receiver == null || (sender == receiver)) {
                out.println("false");
                return;
            }

            LOGGER.info("Creating chat between \n[User]: " + sender + " and [User]: " + receiver);

            isChatCreated = createUserChat(sender, receiver, message_key);
            break;
            GROUP_CHAT:
            String group_name = request.getParameter("group_name");

            // Check if appropriate parameters are passed with the request
            if (group_name == null) {
                out.println("false");
                return;
            }

            LOGGER.info("Creating group chat \n[Owner]: " + sender + ", [Group Name]: " + group_name);

            isChatCreated = createGroupChat(
                    new ArrayList<String>(Array.asList(new String[]{sender})),
                    message_key
            );
            break;
        }

        if (!isChatCreated) {
            out.println("err");
            return;
        }

        out.println("true");
        out.close();
    }

    /**
     * Create chat between two users
     *
     * @param sender User sending message to the Chat
     * @param receiver User to receive the message
     * @param message_key Key used to encrypt and decrypt chat messagaes on client side
     * @return isChatCreated
     */
    protected
    boolean createUserChat (String sender, String receiver, String message_key) {
        System.out.println("Creating a chat");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean isChatCreated = false;
        boolean doesChatExist = doesChatExist(sender, receiver);

        if (doesChatExist) {
            System.out.println("[" + sender + ", " + receiver + "] pair already exists!");
            return false;
        }

        try {
            conn = DatabaseManager.getConnection();

            stmt = conn.prepareStatement("INSERT INTO chat_manager (message_key) VALUES (?)");
            stmt.setString(1, message_key);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            rs.next();
            int chat_id = rs.getInt(1);

            stmt = conn.prepareStatement("INSERT INTO chat_users (chat_id, username) VALUES (?,?)");

            stmt.setInt(1, chat_id);
            stmt.setString(2, sender);
            stmt.addBatch();

            stmt.setInt(1, chat_id);
            stmt.setString(2, receiver);
            stmt.addBatch();

            stmt.executeBatch();

            isChatCreated = true;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        } finally {
            try { rs.close(); } catch (Exception e) {}
            try { stmt.close(); } catch (Exception e) {}
            try { conn.close(); } catch (Exception e) {}
        }

        return isChatCreated;
    }

    protected
    boolean createGroupChat (String group_name, ArrayList<String> users, String message_key) {
        LOGGER.info("Creating a group chat");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean isChatCreated = false;

        try {
            conn = DatabaseManager.getConnection();

            stmt = conn.prepareStatement("INSERT INTO chat_manager (message_key) VALUES (?)");
            stmt.setString(1, message_key);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            rs.next();
            int chat_id = rs.getInt(1);

            stmt = conn.prepareStatement("INSERT INTO chat_groups (chat_id, name) VALUES (?,?)");
            stmt.setInt(1, chat_id);
            stmt.setString(2, group_name);
            stmt.executeUpdate();

            stmt = conn.prepareStatement("INSERT INTO chat_users (chat_id, username) VALUES (?,?)");
            for (String username : users) {
                stmt.setInt(1, chat_id);
                stmt.setString(2, username);
                stmt.addBatch();
            }

            stmt.executeBatch();

            isChatCreated = true;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        } finally {
            try { rs.close(); } catch (Exception e) {}
            try { stmt.close(); } catch (Exception e) {}
            try { conn.close(); } catch (Exception e) {}
        }

        return isChatCreated;
    }

    /**
     * Check if the chat already exists between the users
     *
     * @param sender User sending message to the Chat
     * @param receiver User to receive the message
     * @return doesChatExist
     */
    protected
    boolean doesChatExist (String sender, String receiver) {
        return false;
		/*Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		boolean doesChatExist = false;

		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("SELECT chat_id FROM chat_manager WHERE (user_one=? OR user_two=?) AND (user_one=? OR user_two=?)");
			stmt.setString(1, sender);
			stmt.setString(2, sender);
			stmt.setString(3, receiver);
			stmt.setString(4, receiver);

			rs = stmt.executeQuery();
			if (rs.next()) {
				doesChatExist = true;
			}
		} catch (SQLException e) {
    		LOGGER.severe(e.getMessage());
		} finally {
    		try { stmt.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
		}

		return doesChatExist;*/
    }


    protected static
    final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Get random string
     *
     * @param count Number of random characters to be added to the string
     * @return Random String
     */
    protected
    static String randomKey(int count) {
        StringBuilder builder = new StringBuilder();

        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }

        return builder.toString();
    }
}