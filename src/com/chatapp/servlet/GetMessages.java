package com.chatapp.servlet;

import com.chatapp.format.ChatMessage;
import com.chatapp.util.DatabaseQuery;
import com.chatapp.util.ElasticManager;
import com.chatapp.util.GetJson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.xjson.XJSONException;
import org.json.xjson.XJSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *  - Route: /messages
 *	- GET
 *		[type] Type of messages to get from database
 *		[chat_id] Chat ID corresponding to the users chat
 *		[msg_id] [OPTIONAL] Message ID use dependent on the type of messages required
 *		[username] (FROM SESSION)
 *		(json reply) Array of Chat Messages
 *		(onFail reply) false
 */
@WebServlet(urlPatterns = "/messages")
public class GetMessages extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(GetMessages.class.getName());

	private static final int MESSAGE_TYPE_NEW = 0;
	private static final int MESSAGE_TYPE_OLD = 1;
	private static final int MESSAGE_TYPE_CURRENT = 2;
	
    public GetMessages() {
        super();
    }

    @Override
	protected void
	doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();
		String session_user = null;
		HttpSession session = request.getSession(false);

		int chat_id, type;

		// Check if appropriate parameters are passed with the request
		// Also check if a user session exists
		try {
			chat_id = Integer.parseInt(request.getParameter("chat_id"));
			type = Integer.parseInt(request.getParameter("type"));
			session_user = (String)session.getAttribute("username");
		} catch (NullPointerException e) {
			out.println("false");
			out.close();
			return;
		}

		if ( !DatabaseQuery.isUserAuthorized(session_user, chat_id) ) {
			LOGGER.info("User: " + session_user + " accessed an unauthorized group");
			out.print("false");
			out.close();
			return;
		}

		ArrayList<ChatMessage> chatMessages = getChatMessages(type, chat_id);

		// msg_id (Optional Parameter)
		// Check if msg_id is passed and accordingly execute
		// the getChatMessages method
		if (request.getParameter("timestamp") == null) {
			chatMessages = getChatMessages(type, chat_id);
		} else {
			try {
				chatMessages = getChatMessages(type, chat_id, request.getParameter("timestamp"));
			} catch (Exception e) {
				LOGGER.severe(e.getMessage());
			}
		}

		LOGGER.info("GetMessages request! \nchat_id: " + chat_id + " \ntype: " + type);

		out.println(ChatMessage.getJsonStringArray(chatMessages));
		out.close();
	}

	@Override
	protected void
	doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		doGet(request, response);
	}

	/**
	 * To retrieve chat messages from the database
	 *
	 * @param type Type of the messages needed to fetch from the database
	 * @param chat_id Unique Chat ID corresponding to individual user chats
	 * @return ArrayList<ChatMessages>
	 *					Array of chat messages retrieved from the database
	 */
	private
	ArrayList<ChatMessage> getChatMessages (int type, int chat_id) {
		return getChatMessages(type, chat_id, null);
	}

	/**
	 * To retrieve chat messages from the database
	 *
	 * Three types of messages can be retrieved from the database:
	 *		MESSAGE_TYPE_NEW
	 *			retrieves at most 10 messages
	 *			(messages after the given datetime)
	 *		MESSAGE_TYPE_OLD
	 *			retrieves at most 10 messages
	 *			(messages before the given datetime)
	 *		MESSAGE_TYPE_CURRENT
	 *			retrieves at most 10 latest messages
	 *
	 * @param type Type of the messages needed to fetch from the database
	 * @param chat_id Unique Chat ID corresponding to individual user chats
	 * @param datetime Date and time of the message
	 * @return ArrayList<ChatMessages>
	 *					Array of chat messages retrieved from the database
	 */
	private
	ArrayList<ChatMessage> getChatMessages (int type, int chat_id, String datetime) {
		ArrayList<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
		XJSONObject resObj = null;

		String sort_order = null, time_order = null;

		switch (type) {
			case MESSAGE_TYPE_NEW:
				time_order = "gt";
				sort_order = "asc";
				break;
			case MESSAGE_TYPE_OLD:
				time_order = "lt";
				sort_order = "desc";
				break;
			case MESSAGE_TYPE_CURRENT:
				sort_order = "desc";
				break;
		}

		try {
			XJSONObject reqObj = new XJSONObject(GetJson.from("es-mappings/message.json"));

			reqObj.put("query.bool.must.term.chat_id", chat_id);

			if (time_order != null) {
				// Set Timestamp filter
				reqObj.put("query.bool.filter[0].range.timestamp."+time_order, datetime);

			}

			// Set order of message retrieval
			reqObj.put("sort[0].timestamp.order",sort_order);

			// Set number of messages to extract
			reqObj.put("size", 10);

			System.out.println("Message data: \n" + reqObj.toString());


			resObj = new XJSONObject(ElasticManager.get("/messages/_search", reqObj.toString()));
			System.out.println(resObj.toString());
			JSONArray resArray = (JSONArray) resObj.get("hits.hits");
			for (Object obj : resArray) {
				JSONObject data = ((JSONObject)obj).getJSONObject("_source");
				chatMessages.add(
						new ChatMessage(
							((JSONObject)obj).getString("_id"),
							data.getString("sender"),
							data.getString("message"),
							data.get("timestamp").toString()
						)
				);
			}
		} catch (IOException | XJSONException e) {
			LOGGER.severe(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.severe("Elasticsearch Error: \n" + resObj.toString());
		}

		return chatMessages;
	}
}