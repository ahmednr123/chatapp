package com.chatapp.servlet;

import com.chatapp.util.DatabaseManager;
import com.chatapp.util.ElasticManager;
import com.chatapp.util.GetJson;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;

/**
 *  - Route: /create_chat
 *	- POST
 *      [chat type]	GROUP or USER
 *		[receiver] User to create chat with
 *		[sender] (FROM SESSION)
 *		(String reply) true or false
 */
@WebServlet(urlPatterns = "/create_chat")
public class CreateChat extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(CreateChat.class.getName());

    private static final int USER_CHAT = 0;
    private static final int GROUP_CHAT = 1;

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
        String sender;
        int type;

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
            case USER_CHAT:
                String receiver = request.getParameter("receiver");

                // Check if appropriate parameters are passed with the request
                // and also check if the sender is not creating a chat with himself
                if (receiver == null || (sender.equals(receiver))) {
                    out.println("false");
                    return;
                }

                LOGGER.info("Creating chat between \n[User]: " + sender + " and [User]: " + receiver);

                isChatCreated = createUserChat(sender, receiver, message_key);
                break;

            case GROUP_CHAT:
                String group_name = request.getParameter("group_name");

                // Check if appropriate parameters are passed with the request
                if (group_name == null) {
                    out.print("false");
                    out.close();
                    return;
                }

                LOGGER.info("Creating group chat \n[Owner]: " + sender + ", [Group Name]: " + group_name);

                isChatCreated = createGroupChat(
                        group_name,
                        new String[] {sender},
                        message_key
                );
                break;
        }

        if (!isChatCreated) {
            out.print("err");
            out.close();
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
     * @param message_key Key used to encrypt and decrypt chat messages on client side
     * @return isChatCreated
     */
    private synchronized
    boolean createUserChat (String sender, String receiver, String message_key) {
        LOGGER.info("\nCreating a chat");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean isChatCreated = false;
        boolean doesChatExist = doesChatExist(sender, receiver);

        if (doesChatExist) {
            LOGGER.info("\n[" + sender + ", " + receiver + "] pair already exists!");
            return false;
        }

        try {
            conn = DatabaseManager.getConnection();

            stmt = conn.prepareStatement("INSERT INTO chat_manager (message_key) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
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

    /**
     * Create group chat
     *
     * @param group_name Name of the group
     * @param users Users to be added to the group
     * @param message_key Key used to encrypt and decrypt chat messagaes on client side
     * @return isChatCreated
     */
    private synchronized
    boolean createGroupChat (String group_name, String[] users, String message_key) {
        LOGGER.info("Creating a group chat");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean isChatCreated = false;

        try {
            conn = DatabaseManager.getConnection();

            stmt = conn.prepareStatement("INSERT INTO chat_manager (message_key) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
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
            LOGGER.info("\nmaybe .addBatch() error");
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
    private
    boolean doesChatExist (String sender, String receiver) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		boolean doesChatExist = false;

        try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement("SELECT username from chat_users WHERE chat_id IN (SELECT chat_id from chat_users WHERE chat_id NOT IN (SELECT chat_id FROM chat_groups) AND username=?) AND username!=?");
			stmt.setString(1, sender);
			stmt.setString(2, sender);

			rs = stmt.executeQuery();
			rs.next();
			if (receiver.equals(rs.getString("username"))) {
				doesChatExist = true;
			}
		} catch (SQLException e) {
    		LOGGER.severe(e.getMessage());
		} finally {
            try { rs.close(); } catch (Exception e) {}
    		try { stmt.close(); } catch (Exception e) {}
    		try { conn.close(); } catch (Exception e) {}
		}

		return doesChatExist;
    }

    /**
     * Create a chat index in elasticsearch
     *
     * @param chat_id
     * @return
     */
    private boolean createChatMessageIndex (int chat_id) {
        boolean isIndexCreated = false;
        JSONObject responseObj = null;

        try {
            responseObj = ElasticManager.put("/" + chat_id + "_msgs", GetJson.from("es-mappings/chat_messages.json"));
            System.out.println(responseObj.toString());
            if(responseObj.getString("error") != null) {
                throw new RuntimeException();
            }
            isIndexCreated = true;
        } catch (IOException e) {
            LOGGER.severe("JSON error: \n" + e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.severe(e.getMessage());
        }

        return isIndexCreated;
    }


    private static
    final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Get random string
     *
     * @param count Number of random characters to be added to the string
     * @return Random String
     */
    private
    static String randomKey(int count) {
        StringBuilder builder = new StringBuilder();

        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }

        return builder.toString();
    }
}